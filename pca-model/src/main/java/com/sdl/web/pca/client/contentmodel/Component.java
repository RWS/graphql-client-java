package com.sdl.web.pca.client.contentmodel;

import com.sdl.web.pca.client.contentmodel.enums.ItemType;
import java.util.List;

/**
*Represents a regular component.
*/
public class Component implements ContentComponent,Item {
		private String creationDate;
		private CustomMetaConnection customMetas;
		private String id;
		private String initialPublishDate;
		private int itemId;
		private ItemType itemType;
		private String lastPublishDate;
		private Integer namespaceId;
		private Integer owningPublicationId;
		private int publicationId;
		private Integer schemaId;
		private List<TaxonomyItem> taxonomies;
		private String title;
		private String updatedDate;
		private boolean multiMedia;


		public String getCreationDate()
		{
			return creationDate;
		}
		public void setCreationDate(String creationDate)
		{
			this.creationDate = creationDate;
		}


		public CustomMetaConnection getCustomMetas()
		{
			return customMetas;
		}
		public void setCustomMetas(CustomMetaConnection customMetas)
		{
			this.customMetas = customMetas;
		}


		public String getId()
		{
			return id;
		}
		public void setId(String id)
		{
			this.id = id;
		}


		public String getInitialPublishDate()
		{
			return initialPublishDate;
		}
		public void setInitialPublishDate(String initialPublishDate)
		{
			this.initialPublishDate = initialPublishDate;
		}


		public int getItemId()
		{
			return itemId;
		}
		public void setItemId(int itemId)
		{
			this.itemId = itemId;
		}


		public ItemType getItemType()
		{
			return itemType;
		}
		public void setItemType(ItemType itemType)
		{
			this.itemType = itemType;
		}


		public String getLastPublishDate()
		{
			return lastPublishDate;
		}
		public void setLastPublishDate(String lastPublishDate)
		{
			this.lastPublishDate = lastPublishDate;
		}


		public Integer getNamespaceId()
		{
			return namespaceId;
		}
		public void setNamespaceId(Integer namespaceId)
		{
			this.namespaceId = namespaceId;
		}


		public Integer getOwningPublicationId()
		{
			return owningPublicationId;
		}
		public void setOwningPublicationId(Integer owningPublicationId)
		{
			this.owningPublicationId = owningPublicationId;
		}


		public int getPublicationId()
		{
			return publicationId;
		}
		public void setPublicationId(int publicationId)
		{
			this.publicationId = publicationId;
		}


		public Integer getSchemaId()
		{
			return schemaId;
		}
		public void setSchemaId(Integer schemaId)
		{
			this.schemaId = schemaId;
		}


		public List<TaxonomyItem> getTaxonomies()
		{
			return taxonomies;
		}
		public void setTaxonomies(List<TaxonomyItem> taxonomies)
		{
			this.taxonomies = taxonomies;
		}


		public String getTitle()
		{
			return title;
		}
		public void setTitle(String title)
		{
			this.title = title;
		}


		public String getUpdatedDate()
		{
			return updatedDate;
		}
		public void setUpdatedDate(String updatedDate)
		{
			this.updatedDate = updatedDate;
		}


		public boolean getMultiMedia()
		{
			return multiMedia;
		}
		public void setMultiMedia(boolean multiMedia)
		{
			this.multiMedia = multiMedia;
		}
	
}
