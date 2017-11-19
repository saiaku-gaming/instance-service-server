package com.valhallagame.characterserviceclient.message;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CharacterParameter {
	private String owner;
	
	//Case sensative as it overrides DisplayUsername
	private String characterName;
	
    private String chestItem;

    private String mainhandArmament;
    
    private String offHandArmament;
}
