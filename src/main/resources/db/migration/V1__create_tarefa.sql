CREATE TABLE tarefa (
    id BIGINT NOT NULL AUTO_INCREMENT,
    nome VARCHAR(255),
    data_entrega DATE,
    responsavel VARCHAR(255),
    PRIMARY KEY (id)
) ENGINE=InnoDB;
