package com.valhallagame.characterserviceclient.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Character {
	private String owner;
	private String charactername;
	private String displayCharactername;
}
