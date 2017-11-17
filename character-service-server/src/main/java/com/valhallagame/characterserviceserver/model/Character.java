package com.valhallagame.characterserviceserver.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@Table(name = "character")
public class Character {

    @Id
    @SequenceGenerator(name = "character_id_seq", sequenceName = "character_id_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "character_id_seq")
    @Column(name = "character_id", updatable = false)
    private int id;

    @Column()
    private String owner;

    @Column(unique = true, name = "character_name")
    private String characterName;

    @Column(unique = true, name = "display_character_name")
    private String displayCharacterName;

}
