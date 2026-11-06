(() => {
  const apiBase = window.FASE_API_URL || "http://127.0.0.1:8080";
  const form = document.querySelector("#cadastro-jovem");
  if (!form) return;

  const birthDate = form.elements.dataNascimento;
  const latestBirthDate = new Date();
  latestBirthDate.setFullYear(latestBirthDate.getFullYear() - 16);
  birthDate.max = latestBirthDate.toISOString().slice(0, 10);

  form.addEventListener("submit", async event => {
    event.preventDefault();
    const status = document.querySelector("#cadastro-jovem-status");
    const button = form.querySelector('button[type="submit"]');
    status.hidden = false;
    status.textContent = "Criando sua conta...";
    button.disabled = true;

    const valueOrNull = name => form.elements[name].value.trim() || null;
    try {
      const response = await fetch(`${apiBase}/api/auth/register/youth`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          nome: form.elements.nome.value.trim(),
          email: form.elements.email.value.trim(),
          senha: form.elements.senha.value,
          dataNascimento: form.elements.dataNascimento.value,
          telefone: valueOrNull("telefone"),
          escolaridade: valueOrNull("escolaridade"),
          cidade: valueOrNull("cidade"),
          uf: valueOrNull("uf")
        })
      });
      const result = await response.json();
      if (!response.ok) throw new Error(result.mensagem || "Não foi possível criar a conta.");
      sessionStorage.setItem("fase-admin-token", result.token);
      sessionStorage.setItem("fase-admin-role", result.perfil);
      window.location.assign("conta.html");
    } catch (error) {
      status.textContent = error instanceof TypeError
        ? "Não foi possível conectar à API. Verifique se ela está em execução."
        : error.message;
    } finally {
      button.disabled = false;
    }
  });
})();
