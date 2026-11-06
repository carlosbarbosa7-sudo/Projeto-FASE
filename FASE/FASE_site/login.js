(() => {
  const apiBase = window.FASE_API_URL || "http://127.0.0.1:8080";
  const form = document.querySelector("#admin-login");
  if (!form) return;

  form.addEventListener("submit", async event => {
    event.preventDefault();
    const status = document.querySelector("#login-status");
    const button = form.querySelector('button[type="submit"]');
    status.hidden = false;
    status.textContent = "Verificando acesso...";
    button.disabled = true;

    try {
      const response = await fetch(`${apiBase}/api/auth/login`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          usuario: form.elements.usuario.value.trim(),
          senha: form.elements.senha.value
        })
      });
      const result = await response.json();
      if (!response.ok) throw new Error(result.mensagem || "Não foi possível entrar.");
      sessionStorage.setItem("fase-admin-token", result.token);
      sessionStorage.setItem("fase-admin-role", result.perfil);
      window.location.assign(result.perfil === "ADMIN" ? "admin.html" : "conta.html");
    } catch (error) {
      status.textContent = error instanceof TypeError
        ? "Não foi possível conectar à API. Verifique se ela está em execução."
        : error.message;
    } finally {
      button.disabled = false;
    }
  });
})();
