package com.valhallagame.instanceserviceserver.message;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SessionAndConnectionResponse {
	private String address;
	private int port;
	private String playerSession;
}
