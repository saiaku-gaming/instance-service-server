package com.valhallagame.instanceserviceserver.message;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GetDungeonConnectionParameter {
	private String username;
	private String gameSessionId;
	private String version;
}
