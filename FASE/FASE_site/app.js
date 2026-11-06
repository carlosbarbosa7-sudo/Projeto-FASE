(() => {
  const apiBase = window.FASE_API_URL || "http://127.0.0.1:8080";
  const modalidades = {
    PRESENCIAL: "Presencial",
    HIBRIDO: "Híbrido",
    REMOTO: "Remoto",
    ONLINE: "Online"
  };

  const formatarDinheiro = valor => new Intl.NumberFormat("pt-BR", {
    style: "currency",
    currency: "BRL"
  }).format(valor);

  const localDa = oportunidade => oportunidade.cidade
    ? `${oportunidade.cidade}${oportunidade.uf ? ` - ${oportunidade.uf}` : ""}`
    : (oportunidade.modalidade === "ONLINE" || oportunidade.modalidade === "REMOTO" ? "Online / remoto" : "Não informado");

  const criarTexto = (tag, className, text) => {
    const element = document.createElement(tag);
    if (className) element.className = className;
    element.textContent = text;
    return element;
  };

  const criarCartao = (oportunidade, tipo) => {
    const card = document.createElement("article");
    card.className = tipo === "CURSO" ? "cartao curso" : "cartao item";
    card.dataset.oportunidade = "";
    card.dataset.categoria = oportunidade.categoria || "";
    card.dataset.local = localDa(oportunidade);
    card.dataset.modalidade = oportunidade.modalidade || "";
    card.dataset.instituicao = oportunidade.instituicao || "Instituição não informada";
    card.dataset.requisitos = oportunidade.requisitos || "Não informado.";
    card.dataset.descricao = oportunidade.descricao || "Descrição não informada.";

    const metadados = [oportunidade.categoria, localDa(oportunidade), modalidades[oportunidade.modalidade] || oportunidade.modalidade];
    if (tipo === "VAGA" && oportunidade.tipoVaga) metadados.push(oportunidade.tipoVaga.replaceAll("_", " "));
    if (tipo === "VAGA" && oportunidade.remuneracao !== null) metadados.push(formatarDinheiro(oportunidade.remuneracao));
    if (tipo === "CURSO" && oportunidade.gratuito === true) metadados.push("Gratuito");
    if (tipo === "CURSO" && oportunidade.gratuito === false && oportunidade.valorCurso !== null) metadados.push(formatarDinheiro(oportunidade.valorCurso));

    const information = document.createElement("div");
    information.append(criarTexto("span", "tag", metadados.filter(Boolean).join(" · ")));
    information.append(criarTexto("h2", "", oportunidade.titulo));
    information.append(criarTexto("p", "", oportunidade.descricao || "Descrição não informada."));

    if (oportunidade.cargaHoraria) {
      information.append(criarTexto("p", "", `Carga horária: ${oportunidade.cargaHoraria} horas`));
    }

    const actions = document.createElement("div");
    actions.className = "acoes-oportunidade";
    const detailsButton = document.createElement("button");
    detailsButton.className = "botao";
    detailsButton.type = "button";
    detailsButton.dataset.abrirDetalhes = "";
    detailsButton.textContent = "Ver detalhes";
    actions.append(detailsButton);

    const link = document.createElement("a");
    link.className = "link-oficial";
    link.href = oportunidade.linkExterno;
    link.target = "_blank";
    link.rel = "noopener noreferrer";
    link.textContent = `Acessar canal oficial · ${oportunidade.instituicao || "Instituição"}`;
    actions.append(link);

    const saveButton = document.createElement("button");
    saveButton.className = "botao botao-secundario botao-salvar";
    saveButton.type = "button";
    saveButton.textContent = "Salvar oportunidade";
    saveButton.addEventListener("click", async () => {
      const token = sessionStorage.getItem("fase-admin-token");
      const role = sessionStorage.getItem("fase-admin-role");
      if (!token || role !== "JOVEM") {
        window.location.assign("login.html");
        return;
      }
      saveButton.disabled = true;
      try {
        const response = await fetch(`${apiBase}/api/me/oportunidades-salvas/${oportunidade.id}`, {
          method: "POST",
          headers: { Authorization: `Bearer ${token}` }
        });
        const result = await response.json().catch(() => null);
        if (response.status === 401) {
          sessionStorage.removeItem("fase-admin-token");
          sessionStorage.removeItem("fase-admin-role");
          window.location.assign("login.html");
          return;
        }
        if (!response.ok) throw new Error(result?.mensagem || "Não foi possível salvar a oportunidade.");
        saveButton.textContent = "Salva no seu perfil";
      } catch (error) {
        const message = criarTexto("p", "aviso", error.message);
        saveButton.insertAdjacentElement("afterend", message);
      } finally {
        saveButton.disabled = false;
      }
    });
    actions.append(saveButton);
    card.append(information, actions);
    return card;
  };

  const preencherOpcoes = (select, values) => {
    const placeholder = select.options[0]?.cloneNode(true);
    select.replaceChildren();
    if (placeholder) select.append(placeholder);
    for (const [value, label] of values) {
      const option = document.createElement("option");
      option.value = value;
      option.textContent = label;
      select.append(option);
    }
  };

  const carregarLista = async ({ tipo, formId, listId, countId, emptyId, statusId }) => {
    const form = document.getElementById(formId);
    const list = document.getElementById(listId);
    if (!form || !list) return;

    const count = document.getElementById(countId);
    const empty = document.getElementById(emptyId);
    const status = document.getElementById(statusId);
    const search = form.querySelector('input[type="search"]');
    const category = form.querySelector('[name="categoria"]');
    const location = form.querySelector('[name="local"]');
    const modality = form.querySelector('[name="modalidade"]');
    let opportunities = [];

    count.textContent = "Carregando oportunidades...";
    try {
      const response = await fetch(`${apiBase}/api/oportunidades?tipo=${tipo}`);
      if (!response.ok) throw new Error(`A API respondeu HTTP ${response.status}.`);
      opportunities = await response.json();
      if (!Array.isArray(opportunities)) throw new Error("A API retornou um formato inesperado.");

      preencherOpcoes(category, [...new Set(opportunities.map(item => item.categoria).filter(Boolean))]
        .sort((a, b) => a.localeCompare(b, "pt-BR")).map(value => [value, value]));
      preencherOpcoes(location, [...new Set(opportunities.map(localDa))]
        .sort((a, b) => a.localeCompare(b, "pt-BR")).map(value => [value, value]));
      preencherOpcoes(modality, [...new Set(opportunities.map(item => item.modalidade).filter(Boolean))]
        .sort().map(value => [value, modalidades[value] || value]));

      const filter = () => {
        const term = search.value.trim().toLocaleLowerCase("pt-BR");
        const visible = opportunities.filter(item => {
          const searchable = [item.titulo, item.descricao, item.requisitos, item.categoria, item.instituicao]
            .filter(Boolean).join(" ").toLocaleLowerCase("pt-BR");
          return (!term || searchable.includes(term))
            && (!category.value || item.categoria === category.value)
            && (!location.value || localDa(item) === location.value)
            && (!modality.value || item.modalidade === modality.value);
        });

        list.replaceChildren(...visible.map(item => criarCartao(item, tipo)));
        count.textContent = `${visible.length} ${visible.length === 1 ? "resultado encontrado" : "resultados encontrados"}`;
        empty.hidden = visible.length !== 0;
      };

      form.addEventListener("submit", event => { event.preventDefault(); filter(); });
      form.addEventListener("reset", () => requestAnimationFrame(filter));
      for (const field of form.querySelectorAll("input, select")) {
        field.addEventListener("input", filter);
        field.addEventListener("change", filter);
      }

      const params = new URLSearchParams(window.location.search);
      if (params.has("q")) search.value = params.get("q");
      filter();
    } catch (error) {
      count.textContent = "";
      status.hidden = false;
      status.textContent = `Não foi possível carregar ${tipo === "VAGA" ? "as vagas" : "os cursos"}. Confira se a API está rodando em ${apiBase}.`;
      console.error("Falha ao carregar oportunidades do FASE:", error);
    }
  };

  carregarLista({ tipo: "VAGA", formId: "filtros-vagas", listId: "lista-vagas", countId: "contagem-vagas", emptyId: "sem-resultados-vagas", statusId: "estado-api-vagas" });
  carregarLista({ tipo: "CURSO", formId: "filtros-cursos", listId: "lista-cursos", countId: "contagem-cursos", emptyId: "sem-resultados-cursos", statusId: "estado-api-cursos" });

  const registrationForm = document.querySelector("#form-cadastro-empresa");
  if (registrationForm) {
    registrationForm.addEventListener("submit", async event => {
      event.preventDefault();
      const status = document.querySelector("#status-cadastro");
      const local = registrationForm.elements.cidade.value.match(/^\s*(.+?)\s*[-–]\s*([A-Za-z]{2})\s*$/);
      if (!local) {
        status.textContent = "Informe cidade e estado no formato Manaus - AM.";
        status.hidden = false;
        return;
      }

      const payload = {
        tipo: registrationForm.elements.tipo.value,
        razaoSocial: registrationForm.elements.organizacao.value.trim(),
        cnpj: registrationForm.elements.cnpj.value,
        responsavel: registrationForm.elements.responsavel.value.trim(),
        email: registrationForm.elements.email.value.trim(),
        telefone: registrationForm.elements.telefone.value.trim(),
        site: registrationForm.elements.site.value.trim() || null,
        atuacao: registrationForm.elements.atuacao.value,
        cidade: local[1].trim(),
        uf: local[2].toUpperCase(),
        confirmacao: registrationForm.elements.confirmacao.checked
      };

      status.hidden = false;
      status.textContent = "Enviando solicitação...";
      const submit = registrationForm.querySelector('button[type="submit"]');
      submit.disabled = true;
      try {
        const response = await fetch(`${apiBase}/api/organizacoes`, {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify(payload)
        });
        const result = await response.json();
        if (!response.ok) throw new Error(result.mensagem || result.detail || result.message || result.error || "A solicitação foi recusada pela API.");
        status.textContent = `Solicitação nº ${result.id} recebida. Status: ${result.status}. ${result.mensagem}`;
        registrationForm.reset();
      } catch (error) {
        status.textContent = error instanceof TypeError
          ? "Não foi possível conectar à API. Confira se ela está rodando e tente novamente."
          : error.message;
      } finally {
        submit.disabled = false;
      }
    });
  }

  const dialog = document.querySelector("#detalhes-dialog");
  if (!dialog) return;
  const close = () => dialog.close();
  dialog.querySelector("[data-fechar-dialog]").addEventListener("click", close);
  dialog.addEventListener("click", event => { if (event.target === dialog) close(); });
  document.addEventListener("click", event => {
    const button = event.target.closest("[data-abrir-detalhes]");
    if (!button) return;
    const card = button.closest("[data-oportunidade]");
    if (!card) return;
    dialog.querySelector("#detalhes-titulo").textContent = card.querySelector("h2").textContent;
    dialog.querySelector("#detalhes-meta").textContent = card.querySelector(".tag").textContent;
    dialog.querySelector("#detalhes-descricao").textContent = card.dataset.descricao;
    dialog.querySelector("#detalhes-instituicao").textContent = card.dataset.instituicao;
    dialog.querySelector("#detalhes-requisitos").textContent = card.dataset.requisitos;
    const source = card.querySelector(".link-oficial");
    const link = dialog.querySelector("#detalhes-link");
    link.href = source.href;
    link.textContent = "Consultar no site oficial: " + card.dataset.instituicao;
    dialog.showModal();
  });
})();
