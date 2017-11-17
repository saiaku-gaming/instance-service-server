package com.valhallagame.characterserviceserver.message;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CharacterParameter {
	private String owner;
	private String characterName;
	private String displayCharacterName;
}
