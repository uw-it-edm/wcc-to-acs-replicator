package edu.uw.edm.wcctoacsreplicator.consumers.wccevents.model;

import lombok.Data;


@Data
public class DocumentIndexingMessage {

	private Document document;
	private IndexingType documentChangedType;

	public enum IndexingType {
		create, update, delete,
	}

	public DocumentIndexingMessage() {
	}

	public DocumentIndexingMessage(Document document, IndexingType documentChangedType) {
		this.document = document;
		this.documentChangedType = documentChangedType;
	}

}
