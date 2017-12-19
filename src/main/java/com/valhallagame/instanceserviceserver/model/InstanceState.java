package com.valhallagame.instanceserviceserver.model;

public enum InstanceState {
	STARTING, // instance has been created but not registered itself yet.
	ACTIVE, // Instance is ready to recieve players.
	FINISHING, // No new players should be routed here. But if someone gets in, it should not
				// be removed.
	FINISHED; // The instance is now dead and should be removed from the database

	public String getValue() {
		return this.name();
	}
}
