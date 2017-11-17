package com.valhallagame.characterserviceclient;

import java.util.Optional;

import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.valhallagame.characterserviceclient.model.Character;
import com.valhallagame.characterserviceclient.model.CharacterNameParameter;

public class CharacterServiceClient {
	private static CharacterServiceClient characterServiceClient;

	private String characterServiceServerUrl = "http://localhost:1236";

	private CharacterServiceClient() {
	}

	public static void init(String characterServiceServerUrl) {
		CharacterServiceClient client = get();
		client.characterServiceServerUrl = characterServiceServerUrl;
	}

	public static CharacterServiceClient get() {
		if (characterServiceClient == null) {
			characterServiceClient = new CharacterServiceClient();
		}

		return characterServiceClient;
	}

	public Optional<Character> getCharacter(String characterName) {
		RestTemplate restTemplate = new RestTemplate();
		try {
			return Optional.ofNullable(restTemplate.postForObject(characterServiceServerUrl + "/v1/character/get-character",
					new CharacterNameParameter(characterName), Character.class));
		} catch (RestClientException e) {
			e.printStackTrace();
			return Optional.empty();
		}
	}
}
