(() => {
  const apiBase = window.FASE_API_URL || "http://127.0.0.1:8080";
  const tokenKey = "fase-admin-token";
  const roleKey = "fase-admin-role";
  const token = sessionStorage.getItem(tokenKey);
  if (!token) {
    window.location.replace("login.html");
    return;
  }

  const status = document.querySelector("#conta-status");
  const api = async (path, options = {}) => {
    const response = await fetch(`${apiBase}${path}`, {
      ...options,
      headers: {
        Authorization: `Bearer ${token}`,
        ...(options.body ? { "Content-Type": "application/json" } : {}),
        ...options.headers
      }
    });
    const body = await response.text();
    const result = body ? JSON.parse(body) : null;
    if (response.status === 401) {
      sessionStorage.removeItem(tokenKey);
      sessionStorage.removeItem(roleKey);
      window.location.replace("login.html");
      throw new Error("Sua sessão expirou. Entre novamente.");
    }
    if (!response.ok) throw new Error(result?.mensagem || "A solicitação não pôde ser concluída.");
    return result;
  };

  const showError = error => {
    status.hidden = false;
    status.textContent = error.message;
  };

  const addDetail = (container, label, value) => {
    if (!value) return;
    const paragraph = document.createElement("p");
    const heading = document.createElement("strong");
    heading.textContent = `${label}: `;
    paragraph.append(heading, document.createTextNode(value));
    container.append(paragraph);
  };

  const loadSaved = async () => {
    const area = document.querySelector("#area-salvos");
    const list = document.querySelector("#lista-salvos");
    area.hidden = false;
    try {
      const opportunities = await api("/api/me/oportunidades-salvas");
      list.replaceChildren();
      if (!opportunities.length) {
        const empty = document.createElement("p");
        empty.className = "sem-resultados";
        empty.textContent = "Você ainda não salvou oportunidades.";
        list.append(empty);
        return;
      }
      for (const opportunity of opportunities) {
        const card = document.createElement("article");
        card.className = "cartao";
        const heading = document.createElement("h3");
        heading.textContent = opportunity.titulo;
        const description = document.createElement("p");
        description.textContent = `${opportunity.tipo} · ${opportunity.instituicao} · ${opportunity.categoria}`;
        const remove = document.createElement("button");
        remove.className = "botao botao-secundario";
        remove.type = "button";
        remove.textContent = "Remover dos salvos";
        remove.addEventListener("click", async () => {
          try {
            await api(`/api/me/oportunidades-salvas/${opportunity.id}`, { method: "DELETE" });
            await loadSaved();
          } catch (error) {
            showError(error);
          }
        });
        card.append(heading, description, remove);
        list.append(card);
      }
    } catch (error) {
      showError(error);
    }
  };

  const loadAccount = async () => {
    try {
      const account = await api("/api/me");
      sessionStorage.setItem(roleKey, account.perfil);
      if (account.perfil === "ADMIN") {
        window.location.replace("admin.html");
        return;
      }
      document.querySelector("#conta-boas-vindas").textContent = `Olá, ${account.nome}.`;
      const details = document.querySelector("#dados-perfil");
      details.replaceChildren();
      addDetail(details, "E-mail", account.email);
      if (account.perfil === "JOVEM") {
        document.querySelector("#nav-salvos").hidden = false;
        addDetail(details, "Data de nascimento", account.dataNascimento);
        addDetail(details, "Telefone", account.telefone);
        addDetail(details, "Escolaridade", account.escolaridade);
        addDetail(details, "Cidade", [account.cidade, account.uf].filter(Boolean).join(" - "));
        await loadSaved();
      } else {
        addDetail(details, "Instituição", account.organizacao);
        addDetail(details, "Situação", account.statusOrganizacao);
        addDetail(details, "Telefone", account.telefone);
        addDetail(details, "Localidade", [account.cidade, account.uf].filter(Boolean).join(" - "));
        const note = document.createElement("p");
        note.className = "aviso";
        note.textContent = "O cadastro de oportunidades pela própria instituição ainda não está disponível. Neste protótipo, solicite ao administrador que registre as oportunidades para sua organização.";
        details.append(note);
      }
    } catch (error) {
      showError(error);
    }
  };

  document.querySelector("#form-alterar-senha").addEventListener("submit", async event => {
    event.preventDefault();
    const form = event.currentTarget;
    const button = form.querySelector('button[type="submit"]');
    button.disabled = true;
    try {
      await api("/api/auth/change-password", {
        method: "POST",
        body: JSON.stringify({
          senhaAtual: form.elements.senhaAtual.value,
          novaSenha: form.elements.novaSenha.value
        })
      });
      status.hidden = false;
      status.textContent = "Senha atualizada.";
      form.reset();
    } catch (error) {
      showError(error);
    } finally {
      button.disabled = false;
    }
  });

  document.querySelector("#sair-conta").addEventListener("click", async () => {
    try {
      await api("/api/auth/logout", { method: "POST" });
    } catch (error) {
      showError(error);
    } finally {
      sessionStorage.removeItem(tokenKey);
      sessionStorage.removeItem(roleKey);
      window.location.replace("index.html");
    }
  });

  loadAccount();
})();
