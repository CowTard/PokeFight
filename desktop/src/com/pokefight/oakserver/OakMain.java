package com.pokefight.oakserver;

import java.awt.Image;
import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;
import com.pokejava.Sprite;

public class OakMain {
	public static void main(String[] args) {
		Server server = new Server();
		server.start();
		try {
			server.bind(54555, 54777);
		} catch (IOException e) {
			e.printStackTrace();
		}

		server.addListener(new Listener() {
			public void receive(Connection conn, Object obj) {
				if (obj instanceof Pok�monRequest) {			
					Pok�monRequest req = (Pok�monRequest) obj;
					ResourceResponse resp = new ResourceResponse(req);
					
					try {
						resp.getResponse(); // Passar para Pok�mon
					} catch (OakServerException e) {
						com.pokejava.Pokemon newPokemon = new com.pokejava.Pokemon(req.getId());
						Sprite newPokemonSprite = new Sprite(req.getId());

						Image spriteImg = null;
						try {
							URL url = new URL("http://www.pokeapi.co" + newPokemonSprite.getImage());
							spriteImg = ImageIO.read(url);
						} catch (IOException i) {
						}
					}
				}
			}
		});
	}
}
