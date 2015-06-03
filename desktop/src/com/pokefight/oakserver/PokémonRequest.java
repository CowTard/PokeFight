package com.pokefight.oakserver;

public class Pok�monRequest extends ResourceRequest {
	private int id;
	
	Pok�monRequest(int id) {
		this.id = id;
		apiPath = "pokemon/" + id;
	}
	
	public int getId() {
		return id;
	}
}
