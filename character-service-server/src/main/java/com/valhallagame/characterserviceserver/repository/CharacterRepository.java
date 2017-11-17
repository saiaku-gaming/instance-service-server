package com.valhallagame.characterserviceserver.repository;

import java.util.List;
import java.util.Optional;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.valhallagame.characterserviceserver.model.Character;

public interface CharacterRepository extends JpaRepository<Character, Integer> {
	public Optional<Character> findByCharacterName(String characterName);
	
	public List<Character> findByOwner(String owner);

    @Transactional
    @Modifying
    @Query(value = "INSERT INTO selected_character (person_id, character_name) "
    		+ " VALUES (:person_id, :character_name)"
    		+ "ON CONFLICT DO UPDATE SET character_name = :character_name", nativeQuery = true)
	public void setSelectedCharacter(@Param("person_id") String owner, @Param("character_name")  String characterName);
    
    @Transactional
    @Modifying
    @Query(value = "SELECT c.* from character c join selected_character sc USING (person_id, character_name) where sc.person_id = :person_id", nativeQuery = true)
	public Optional<Character> getSelectedCharacter(@Param("person_id") String owner);
}
