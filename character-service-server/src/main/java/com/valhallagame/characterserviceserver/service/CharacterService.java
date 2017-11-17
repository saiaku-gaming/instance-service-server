package com.valhallagame.characterserviceserver.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.valhallagame.characterserviceserver.model.Character;
import com.valhallagame.characterserviceserver.repository.CharacterRepository;

@Service
public class CharacterService {
	@Autowired
	private CharacterRepository characterRepository;

	public Character saveCharacter(Character character) {
		return characterRepository.save(character);
	}

	public Optional<Character> getCharacter(String characterName) {
		return characterRepository.findByCharacterName(characterName);
	}
	
	public List<Character> getCharacters(String username) {
		return characterRepository.findByOwner(username);
	}

	public void setSelectedCharacter(String owner, String characterName) {
		characterRepository.setSelectedCharacter(owner, characterName);
	}
	
	public Optional<Character> getSelectedCharacter(String owner) {
		return characterRepository.getSelectedCharacter(owner);
	}

	public void deleteCharacter(Character local) {
		characterRepository.delete(local);
	}
}
