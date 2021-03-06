package com.pokefight.oakserver;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;

import org.apache.http.HttpStatus;
import org.apache.http.client.fluent.Form;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.fluent.Response;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;
import com.pokefight.resources.Pokémon;
import com.pokejava.Move;
import com.pokejava.Pokemon;
import com.pokejava.Sprite;

public class OakMain {
	public static void main(String[] args) {
		Server server = new Server();

		Kryo kryo = server.getKryo();
		kryo.register(PokémonRequest.class);
	    kryo.register(Pokémon.class);
		kryo.register(MoveRequest.class);
	    kryo.register(com.pokefight.resources.Move.class);
		kryo.register(PokémonMoveRequest.class);
		kryo.register(ArrayList.class);
		
		try {
			server.start();
			server.bind(54555, 54777);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		server.addListener(new Listener() {
			@Override
			public void connected(Connection conn) {
				System.out.println("[OakServer] Ligado!");
			}
			@Override
			public void disconnected(Connection conn) {
				System.out.println("[OakServer] Desligado.");
			}
			@Override
			public void received(Connection conn, Object obj) {
				System.out.println("[OakServer] Recebeu " + obj.getClass());

				if (obj instanceof PokémonRequest) {
					int id = 0;
					String name = "";
					String sprite = "";
					int attack = 0;
					int defense = 0;
					int maxHp = 0;

					PokémonRequest req = (PokémonRequest) obj;
					ResourceResponse resp = new ResourceResponse(req);

					try {
						JSONObject jsonResp = resp.getResponseObject();

						id = jsonResp.getInt("pkmnId");
						name = jsonResp.getString("name");
						sprite = jsonResp.getString("sprite");
						attack = jsonResp.getInt("attack");
						defense = jsonResp.getInt("defense");
						maxHp = jsonResp.getInt("maxHp");
					} catch (OakServerException e) {
						id = req.getId();
						Pokemon newPokemon = new Pokemon(id);
						name = newPokemon.getName();
						Sprite newPokemonSprite = new Sprite(id);
						attack = newPokemon.getAttack();
						defense = newPokemon.getDefense();
						maxHp = newPokemon.getHP();

						BufferedImage spriteImg = null;
						try {
							URL url = new URL("http://www.pokeapi.co" + newPokemonSprite.getImage());
							spriteImg = ImageIO.read(url);
						} catch (IOException i) {
							i.printStackTrace();
						}

						try {
							sprite = new String(((DataBufferByte) spriteImg.getData().getDataBuffer()).getData(), "ISO-8859-1");
						} catch (UnsupportedEncodingException e1) {
							e1.printStackTrace();
						}

						Map<String, String> jsonParameters = new HashMap<String, String>();
						jsonParameters.put("pkmnId", new Integer(id).toString());
						jsonParameters.put("name", name);
						jsonParameters.put("sprite", sprite);
						jsonParameters.put("attack", new Integer(attack).toString());
						jsonParameters.put("defense", new Integer(defense).toString());						
						jsonParameters.put("maxHp", new Integer(maxHp).toString());
						JSONObject newPokemonJson = new JSONObject(jsonParameters);

						postToHttp(req.getApiPath(), newPokemonJson);
					} catch (Exception e) {
						e.printStackTrace();
					}

					conn.sendUDP(new Pokémon(id, name, sprite, attack, defense, maxHp));
					return;
				} else if (obj instanceof MoveRequest) {
					int id = 0;
					String name = "";
					int power = 0;

					MoveRequest req = (MoveRequest) obj;
					ResourceResponse resp = new ResourceResponse(req);

					try {
						JSONObject jsonResp = resp.getResponseObject();

						id = jsonResp.getInt("moveId");
						name = jsonResp.getString("name");
						power = jsonResp.getInt("power");
					} catch (OakServerException e) {
						Move newMove = new com.pokejava.Move(req.getId());
						name = newMove.getName();
						power = newMove.getPower();

						Map<String, String> jsonParameters = new HashMap<String, String>();
						jsonParameters.put("moveId", new Integer(id).toString());
						jsonParameters.put("name", name);
						jsonParameters.put("power", new Integer(power).toString());
						JSONObject newMoveJson = new JSONObject(jsonParameters);

						postToHttp(req.getApiPath(), newMoveJson);
					} catch (Exception e) {
						e.printStackTrace();
					}

					conn.sendUDP(new com.pokefight.resources.Move(id, name, power));
					return;
				} else if (obj instanceof PokémonMoveRequest) {
					ArrayList<Integer> pokemonMoves = new ArrayList<Integer>();

					PokémonMoveRequest req = (PokémonMoveRequest) obj;
					ResourceResponse resp = new ResourceResponse(req);

					try {
						JSONArray jsonResp = resp.getResponseArray();

						for (int i = 0; i < jsonResp.length(); ++i) {
							JSONObject moveJson = jsonResp.getJSONObject(i);
							int moveId = moveJson.getInt("moveId");
							pokemonMoves.add(moveId);
						}
					} catch (OakServerException e) {
						Pokemon pokemon = new Pokemon(req.getPokemonId());

						for (Move move : pokemon.getMoves()) {
							int moveId = move.getID();
							pokemonMoves.add(moveId);
						}

						for (Integer moveId : pokemonMoves) {
							Map<String, String> moveJsonMap = new HashMap<String, String>();
							moveJsonMap.put(new Integer(req.getPokemonId()).toString(), moveId.toString());
							JSONObject moveJson = new JSONObject(moveJsonMap);
							postToHttp(req.getApiPath(), moveJson);
						}
					} catch (Exception e) {
						e.printStackTrace();
					}

					conn.sendUDP(pokemonMoves);
					return;
				}
			}
		});
	}

	private static void postToHttp(String apiUrl, JSONObject postArguments) {
		try {
			Form toSend = Form.form();
			for (String key : JSONObject.getNames(postArguments)) {
				try {
					toSend.add(key, postArguments.getString(key));
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}			

			Response serverResponse = Request.Post("localhost/api/" + apiUrl)
					.bodyForm(toSend.build())
					.execute();
			if (serverResponse.returnResponse().getStatusLine().getStatusCode() == HttpStatus.SC_NOT_FOUND) {
				try {
					throw new OakServerException();
				} catch (OakServerException e) {
					e.printStackTrace();
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
