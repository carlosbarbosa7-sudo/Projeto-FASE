(() => {
  const apiBase = window.FASE_API_URL || "http://127.0.0.1:8080";
  const tokenKey = "fase-admin-token";
  const token = sessionStorage.getItem(tokenKey);
  if (!token) {
    window.location.replace("login.html");
    return;
  }

  const status = document.querySelector("#admin-status");
  const api = async (path, options = {}) => {
    const response = await fetch(`${apiBase}${path}`, {
      ...options,
      headers: {
        "Authorization": `Bearer ${token}`,
        ...(options.body ? { "Content-Type": "application/json" } : {}),
        ...options.headers
      }
    });
    const body = await response.text();
    const result = body ? JSON.parse(body) : null;
    if (response.status === 401) {
      sessionStorage.removeItem(tokenKey);
      window.location.replace("login.html");
      throw new Error("Sua sessão expirou. Entre novamente.");
    }
    if (!response.ok) throw new Error(result?.mensagem || "A solicitação não pôde ser concluída.");
    return result;
  };

  const text = (tag, className, value) => {
    const element = document.createElement(tag);
    if (className) element.className = className;
    element.textContent = value || "";
    return element;
  };

  const button = (label, className, action) => {
    const element = document.createElement("button");
    element.type = "button";
    element.className = className;
    element.textContent = label;
    element.addEventListener("click", action);
    return element;
  };

  const showError = error => {
    status.hidden = false;
    status.textContent = error.message;
  };

  const review = async ({ path, id, result, reason, email }) => {
    const message = reason ? window.prompt("Informe o motivo da recusa:") : "Cadastro conferido pelo administrador";
    if (reason && message === null) return;
    const temporaryPassword = result === "APROVADA" && path === "/api/admin/organizacoes"
      ? createTemporaryPassword()
      : null;
    try {
      await api(`${path}/${id}/status`, {
        method: "PATCH",
        body: JSON.stringify({
          status: result,
          ...(reason ? { motivo: message.trim() } : { motivo: message }),
          ...(temporaryPassword ? { senhaProvisoria: temporaryPassword } : {})
        })
      });
      status.hidden = false;
      if (temporaryPassword) {
        status.textContent = `Instituição aprovada. Login: ${email}. Senha provisória: ${temporaryPassword}. Anote e entregue com segurança à instituição; ela poderá alterá-la na área da conta.`;
      } else {
        status.textContent = result === "APROVADA" || result === "PUBLICADA"
          ? "Solicitação aprovada."
          : "Solicitação recusada.";
      }
      await Promise.all([loadOrganizations(), loadOpportunities()]);
    } catch (error) {
      showError(error);
    }
  };

  const createTemporaryPassword = () => {
    const bytes = crypto.getRandomValues(new Uint8Array(24));
    return btoa(String.fromCharCode(...bytes))
      .replaceAll("+", "-")
      .replaceAll("/", "_")
      .replaceAll("=", "");
  };

  const loadOrganizations = async () => {
    const list = document.querySelector("#lista-instituicoes");
    const count = document.querySelector("#contagem-instituicoes");
    count.textContent = "Carregando...";
    try {
      const organizations = await api("/api/admin/organizacoes/pendentes");
      list.replaceChildren();
      count.textContent = `${organizations.length} ${organizations.length === 1 ? "solicitação pendente" : "solicitações pendentes"}`;
      if (!organizations.length) {
        list.append(text("p", "sem-resultados", "Não há instituições aguardando análise."));
        return;
      }
      for (const organization of organizations) {
        const card = text("article", "cartao");
        card.append(text("span", "tag", organization.tipo));
        card.append(text("h3", "", organization.razaoSocial));
        card.append(text("p", "", `CNPJ: ${organization.cnpj} · ${organization.cidade} - ${organization.uf}`));
        card.append(text("p", "", `Responsável: ${organization.responsavel} · ${organization.email} · ${organization.telefone}`));
        let safeSite;
        try {
          const parsedSite = new URL(organization.site);
          if (parsedSite.protocol === "http:" || parsedSite.protocol === "https:") safeSite = parsedSite.href;
        } catch {
          safeSite = null;
        }
        if (safeSite) {
          const site = document.createElement("a");
          site.className = "link-oficial";
          site.href = safeSite;
          site.target = "_blank";
          site.rel = "noopener noreferrer";
          site.textContent = "Site institucional";
          card.append(site);
        }
        const actions = text("div", "acoes-admin");
        actions.append(button("Aprovar", "botao", () => review({
          path: "/api/admin/organizacoes", id: organization.id, result: "APROVADA", email: organization.email
        })));
        actions.append(button("Recusar", "botao botao-recusar", () => review({
          path: "/api/admin/organizacoes", id: organization.id, result: "RECUSADA", reason: true
        })));
        card.append(actions);
        list.append(card);
      }
    } catch (error) {
      count.textContent = "";
      showError(error);
    }
  };

  const loadOpportunities = async () => {
    const list = document.querySelector("#lista-oportunidades");
    const count = document.querySelector("#contagem-oportunidades");
    count.textContent = "Carregando...";
    try {
      const opportunities = await api("/api/admin/oportunidades/pendentes");
      list.replaceChildren();
      count.textContent = `${opportunities.length} ${opportunities.length === 1 ? "oportunidade pendente" : "oportunidades pendentes"}`;
      if (!opportunities.length) {
        list.append(text("p", "sem-resultados", "Não há oportunidades aguardando revisão."));
        return;
      }
      for (const item of opportunities) {
        const opportunity = item.oportunidade;
        const card = text("article", "cartao");
        card.append(text("span", "tag", `${opportunity.tipo} · ${opportunity.categoria || "Sem categoria"}`));
        card.append(text("h3", "", opportunity.titulo));
        card.append(text("p", "", `Instituição: ${opportunity.instituicao || "Não informada"} · ${opportunity.cidade || "Online"}${opportunity.uf ? ` - ${opportunity.uf}` : ""}`));
        card.append(text("p", "", opportunity.descricao));
        if (opportunity.requisitos) card.append(text("p", "", `Requisitos: ${opportunity.requisitos}`));
        const actions = text("div", "acoes-admin");
        actions.append(button("Publicar", "botao", () => review({
          path: "/api/admin/oportunidades", id: item.id, result: "PUBLICADA"
        })));
        actions.append(button("Recusar", "botao botao-recusar", () => review({
          path: "/api/admin/oportunidades", id: item.id, result: "RECUSADA", reason: true
        })));
        card.append(actions);
        list.append(card);
      }
    } catch (error) {
      count.textContent = "";
      showError(error);
    }
  };

  document.querySelector("#atualizar-instituicoes").addEventListener("click", loadOrganizations);
  document.querySelector("#atualizar-oportunidades").addEventListener("click", loadOpportunities);
  document.querySelector("#admin-logout").addEventListener("click", async () => {
    try {
      await api("/api/auth/logout", { method: "POST" });
    } catch (error) {
      showError(error);
    } finally {
      sessionStorage.removeItem(tokenKey);
      window.location.replace("login.html");
    }
  });

  Promise.all([loadOrganizations(), loadOpportunities()]);
})();
