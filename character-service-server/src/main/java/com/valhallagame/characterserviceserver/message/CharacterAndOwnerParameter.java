package com.valhallagame.characterserviceserver.message;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CharacterAndOwnerParameter {
	private String characterName;
	private String owner;
}
