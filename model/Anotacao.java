// START OF FILE: Anotacao.java
package model;

public class Anotacao {
    private int id;
    private String titulo;
    private String descricao;
    private String status; // Usado para Kanban (AFazer, Fazendo, Feito). Pode ser vazio para Comum.
    private int listId; // ID da lista a que pertence (de user_lists)
    private String prioridade; // Valores serão "Pouco importante", "Importante", "Muito importante"
    private boolean isConcluidaVisual; // Persistido no BD para o estado do checkbox

    // Construtor para criar novas anotações (sem ID, com isConcluidaVisual padrão)
    public Anotacao(String titulo, String descricao, String status, int listId, String prioridade) {
        this.titulo = titulo;
        this.descricao = descricao;
        this.status = status;
        this.listId = listId;
        this.prioridade = prioridade;
        this.isConcluidaVisual = false; // Valor padrão inicial para novas anotações (não concluída)
    }

    // Construtor com isConcluidaVisual explícito (para criação ou movimento)
    public Anotacao(String titulo, String descricao, String status, int listId, String prioridade, boolean isConcluidaVisual) {
        this(titulo, descricao, status, listId, prioridade); // Chama o construtor acima
        this.isConcluidaVisual = isConcluidaVisual; // Atribui o valor passado
    }

    // Construtor completo (para carregar do BD)
    public Anotacao(int id, String titulo, String descricao, String status, int listId, String prioridade, boolean isConcluidaVisual) {
        this(titulo, descricao, status, listId, prioridade, isConcluidaVisual); // Chama o construtor de 6 parâmetros
        this.id = id;
    }

    // Getters
    public int getId() { return id; }
    public String getTitulo() { return titulo; }
    public String getDescricao() { return descricao; }
    public String getStatus() { return status; }
    public int getListId() { return listId; }
    public String getPrioridade() { return prioridade; }
    public boolean isConcluidaVisual() { return isConcluidaVisual; }

    // Setters
    public void setId(int id) { this.id = id; }
    public void setTitulo(String titulo) { this.titulo = titulo; }
    public void setDescricao(String descricao) { this.descricao = descricao; }
    public void setStatus(String status) { this.status = status; }
    public void setListId(int listId) { this.listId = listId; }
    public void setPrioridade(String prioridade) { this.prioridade = prioridade; }
    public void setConcluidaVisual(boolean concluidaVisual) { this.isConcluidaVisual = concluidaVisual; }
}
// END OF FILE: Anotacao.java