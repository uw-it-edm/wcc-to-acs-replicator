package edu.uw.edm.wcctoacsreplicator.consumers.wccevents.model;

import lombok.Data;


@Data
public class DocumentIndexingMessage {
	private Document document;
	private IndexingType indexingType;

	public enum IndexingType {
		create, update, delete,
	}

	public DocumentIndexingMessage() {
	}

	public DocumentIndexingMessage(Document document, IndexingType indexingType) {
		this.document = document;
		this.indexingType = indexingType;
	}

}
