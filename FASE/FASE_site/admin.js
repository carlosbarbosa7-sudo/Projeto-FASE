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
    let result = null;
    try {
      result = body ? JSON.parse(body) : null;
    } catch {
      if (!response.ok) throw new Error(`A API respondeu HTTP ${response.status} em ${path}.`);
      throw new Error(`A API retornou uma resposta inválida em ${path}.`);
    }
    if (response.status === 401) {
      sessionStorage.removeItem(tokenKey);
      window.location.replace("login.html");
      throw new Error("Sua sessão expirou. Entre novamente.");
    }
    if (!response.ok) throw new Error(`${result?.mensagem || "A solicitação não pôde ser concluída."} (HTTP ${response.status}, ${path})`);
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
      await renderDashboard();
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

  const renderDashboard = async () => {
    const updated = document.querySelector("#dashboard-atualizacao");
    updated.textContent = "Atualizando indicadores...";
    try {
      const published = await api("/api/oportunidades");
      const [pendingOrganizationsResult, pendingOpportunitiesResult, approvedResult, rejectedResult] = await Promise.allSettled([
        api("/api/admin/organizacoes/pendentes"),
        api("/api/admin/oportunidades/pendentes"),
        api("/api/admin/organizacoes?status=APROVADA"),
        api("/api/admin/organizacoes?status=RECUSADA")
      ]);
      const pendingOrganizationsCount = pendingOrganizationsResult.status === "fulfilled" ? pendingOrganizationsResult.value.length : "—";
      const pendingOpportunitiesCount = pendingOpportunitiesResult.status === "fulfilled" ? pendingOpportunitiesResult.value.length : "—";
      const approvedCount = approvedResult.status === "fulfilled" ? approvedResult.value.length : "—";
      const rejectedCount = rejectedResult.status === "fulfilled" ? rejectedResult.value.length : "—";
      const jobs = published.filter(item => item.tipo === "VAGA").length;
      const courses = published.filter(item => item.tipo === "CURSO").length;
      const metrics = [
        ["Oportunidades publicadas", published.length, "Disponiveis no portal"],
        ["Vagas", jobs, "Publicadas"],
        ["Cursos", courses, "Publicados"],
        ["Instituições aprovadas", approvedCount, approvedResult.status === "fulfilled" ? "Parceiras aprovadas" : "Reinicie a API para carregar"],
        ["Instituições recusadas", rejectedCount, rejectedResult.status === "fulfilled" ? "Solicitações recusadas" : "Reinicie a API para carregar"],
        ["Oportunidades pendentes", pendingOpportunitiesCount, pendingOpportunitiesResult.status === "fulfilled" ? "Aguardando revisão" : "API indisponível"],
        ["Instituições pendentes", pendingOrganizationsCount, pendingOrganizationsResult.status === "fulfilled" ? "Aguardando análise" : "API indisponível"]
      ];
      const metricGrid = document.querySelector("#dashboard-indicadores");
      metricGrid.replaceChildren(...metrics.map(([label, value, note]) => {
        const card = text("article", "cartao indicador-card");
        card.append(text("span", "indicador-label", label), text("strong", "indicador-valor", String(value)), text("span", "indicador-nota", note));
        return card;
      }));

      const typeCounts = [["Vagas", jobs, "#1769aa"], ["Cursos", courses, "#29a38b"]];
      const total = Math.max(published.length, 1);
      const jobEnd = jobs / total * 100;
      const donut = document.querySelector("#grafico-tipos");
      donut.style.background = published.length
        ? `conic-gradient(#1769aa 0% ${jobEnd}%, #29a38b ${jobEnd}% 100%)`
        : "conic-gradient(#dce9ee 0% 100%)";
      donut.setAttribute("aria-label", `Oportunidades publicadas: ${jobs} vagas e ${courses} cursos`);
      const legend = document.querySelector("#legenda-tipos");
      legend.replaceChildren(...typeCounts.map(([label, value, color]) => {
        const row = text("li", "", "");
        const marker = text("span", "legenda-marcador", "");
        marker.style.backgroundColor = color;
        row.append(marker, text("span", "", label), text("strong", "", String(value)));
        return row;
      }));

      const categoryCounts = new Map();
      for (const item of published) {
        const name = item.categoria || "Sem categoria";
        categoryCounts.set(name, (categoryCounts.get(name) || 0) + 1);
      }
      const categories = [...categoryCounts].sort((a, b) => b[1] - a[1] || a[0].localeCompare(b[0], "pt-BR")).slice(0, 6);
      const maxCategory = Math.max(...categories.map(([, value]) => value), 1);
      const bars = document.querySelector("#grafico-categorias");
      if (!categories.length) {
        bars.replaceChildren(text("p", "grafico-vazio", "Ainda nao ha oportunidades publicadas para exibir."));
      } else {
        bars.replaceChildren(...categories.map(([name, value]) => {
          const row = text("div", "barra-categoria", "");
          const heading = text("div", "barra-cabecalho", "");
          heading.append(text("span", "", name), text("strong", "", String(value)));
          const track = text("div", "barra-trilho", "");
          const fill = text("span", "barra-preenchimento", "");
          fill.style.width = `${value / maxCategory * 100}%`;
          track.append(fill);
          row.append(heading, track);
          return row;
        }));
      }
      updated.textContent = `Atualizado em ${new Intl.DateTimeFormat("pt-BR", { dateStyle: "short", timeStyle: "short" }).format(new Date())}.`;
    } catch (error) {
      updated.textContent = "Não foi possível carregar os indicadores. Confira a conexão com a API.";
      showError(error);
    }
  };

  let statusInstituicoes = "PENDENTE";
  const loadOrganizations = async (requestedStatus = statusInstituicoes) => {
    const list = document.querySelector("#lista-instituicoes");
    const count = document.querySelector("#contagem-instituicoes");
    const labels = { PENDENTE: "pendentes", APROVADA: "aprovadas", RECUSADA: "recusadas" };
    count.textContent = "Carregando...";
    try {
      const organizations = await api(`/api/admin/organizacoes?status=${requestedStatus}`);
      list.replaceChildren();
      const noun = organizations.length === 1 ? "instituição" : "instituições";
      count.textContent = `${organizations.length} ${noun} ${labels[requestedStatus]}`;      if (!organizations.length) {
        const emptyMessages = {
          PENDENTE: "Não há instituições aguardando análise.",
          APROVADA: "Ainda não há instituições aprovadas.",
          RECUSADA: "Ainda não há solicitações recusadas."
        };
        list.append(text("p", "sem-resultados", emptyMessages[requestedStatus]));
        return;
      }
      for (const organization of organizations) {
        const card = text("article", "cartao");
        card.append(text("span", `tag status-instituicao status-${organization.status.toLowerCase()}`, organization.status));
        card.append(text("h3", "", organization.razaoSocial));
        card.append(text("p", "", `CNPJ: ${organization.cnpj} · ${organization.cidade} - ${organization.uf}`));
        card.append(text("p", "", `Responsável: ${organization.responsavel} · ${organization.email} · ${organization.telefone}`));
        if (organization.analisadaEm) {
          card.append(text("p", "instituicao-data", `Analisada em ${new Intl.DateTimeFormat("pt-BR").format(new Date(organization.analisadaEm))}`));
        }
        if (organization.motivoDecisao) card.append(text("p", "instituicao-motivo", `Motivo: ${organization.motivoDecisao}`));
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
        if (requestedStatus === "PENDENTE") {
          const actions = text("div", "acoes-admin");
          actions.append(button("Aprovar", "botao", () => review({
            path: "/api/admin/organizacoes", id: organization.id, result: "APROVADA", email: organization.email
          })));
          actions.append(button("Recusar", "botao botao-recusar", () => review({
            path: "/api/admin/organizacoes", id: organization.id, result: "RECUSADA", reason: true
          })));
          card.append(actions);
        }
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

  document.querySelector("#atualizar-instituicoes").addEventListener("click", () => loadOrganizations());
  for (const tab of document.querySelectorAll(".aba-instituicao")) {
    tab.addEventListener("click", () => {
      statusInstituicoes = tab.dataset.status;
      document.querySelector("#lista-instituicoes").setAttribute("aria-labelledby", tab.id);
      for (const other of document.querySelectorAll(".aba-instituicao")) {
        const active = other === tab;
        other.classList.toggle("ativa", active);
        other.setAttribute("aria-selected", String(active));
      }
      loadOrganizations(statusInstituicoes);
    });
  }
  document.querySelector("#atualizar-oportunidades").addEventListener("click", loadOpportunities);
  document.querySelector("#atualizar-dashboard").addEventListener("click", renderDashboard);
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

  Promise.all([loadOrganizations(), loadOpportunities(), renderDashboard()]);
})();

