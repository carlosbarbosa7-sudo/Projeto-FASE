package br.com.fase.api.admin;

import br.com.fase.api.oportunidade.Categoria;

public record CategoriaAdminResponse(Long id, String tipo, String nome) {
    public static CategoriaAdminResponse de(Categoria categoria) {
        return new CategoriaAdminResponse(categoria.getId(), categoria.getTipo(), categoria.getNome());
    }
}
