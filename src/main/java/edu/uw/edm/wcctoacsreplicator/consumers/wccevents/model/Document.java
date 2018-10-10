package edu.uw.edm.wcctoacsreplicator.consumers.wccevents.model;

import java.util.HashMap;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class Document {
	private String id;
	private String label;
	private Map<String, Object> metadata = new HashMap<>();

	public Document() {
	}

	@SuppressWarnings("unchecked")
	public Document(Map<String, Object> data) {
		this.id = data.get("id").toString();
		this.label = data.get("label").toString();
		if (data.containsKey("metadata")) {
			this.metadata = (Map<String, Object>) data.get("metadata");
		}
	}


	public Map<String, Object> asMap() {
		HashMap<String, Object> map = new HashMap<>();

		map.put("id", id);
		map.put("label", label);
		map.put("metadata", metadata);

		return map;
	}


}
