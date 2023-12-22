package foo;
import java.util.*;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;


import com.google.api.server.spi.auth.common.User;
import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiMethod.HttpMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.api.server.spi.config.Named;
import com.google.api.server.spi.config.Nullable;
import com.google.api.server.spi.response.CollectionResponse;
import com.google.api.server.spi.response.UnauthorizedException;
import com.google.api.server.spi.auth.EspAuthenticator;

import com.google.appengine.api.datastore.Cursor;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.PropertyProjection;
import com.google.appengine.api.datastore.PreparedQuery.TooManyResultsException;
import com.google.appengine.api.datastore.Query.CompositeFilter;
import com.google.appengine.api.datastore.Query.CompositeFilterOperator;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.appengine.api.datastore.QueryResultList;
import com.google.appengine.api.datastore.Transaction;

@Api(name = "myApi",
     version = "v1",
     audiences = "1086302335339-m9ef92cv85rfgi6pb872ii1onead5504.apps.googleusercontent.com",
  	 clientIds = {"1086302335339-m9ef92cv85rfgi6pb872ii1onead5504.apps.googleusercontent.com",
        "1086302335339-m9ef92cv85rfgi6pb872ii1onead5504.apps.googleusercontent.com"},
     namespace =
     @ApiNamespace(
		   ownerDomain = "helloworld.example.com",
		   ownerName = "helloworld.example.com",
		   packagePath = "")
     )

public class ScoreEndpoint {


	@ApiMethod(name = "hello", httpMethod = HttpMethod.GET)
	public User Hello(User user) throws UnauthorizedException {
        if (user == null) {
			throw new UnauthorizedException("Invalid credentials");
		}
        System.out.println("Yeah:"+user.toString());
		return user;
	} 

	
	

    @ApiMethod(name = "addPetition", httpMethod = HttpMethod.POST)
    public Entity addPetition(PostPetition petition) throws UnauthorizedException {
        Entity e = new Entity("Petition"); // Création d'une entité pour la pétition
        e.setProperty("auteur", petition.auteur);
        e.setProperty("title", petition.title);
        e.setProperty("description", petition.description);
        e.setProperty("creatorId", petition.creatorId);
        e.setProperty("status", petition.status);
        e.setProperty("tags", petition.tags);
        e.setProperty("creationDate", new Date()); // Date de création de la pétition
    
        List<String> signatures = new ArrayList<>();
        signatures.add(petition.creatorId); // Ajout de l'ID du créateur à la liste des signatures
        e.setProperty("signatures", signatures);
        e.setProperty("numberOfSignatures", signatures.size());
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        Key petitionKey = datastore.put(e); // Stockage de l'entité de la pétition dans Datastore avec une clé automatique
    
        e.setProperty("ID_pet", petitionKey.getId()); // Obtenez l'ID auto-généré et enregistrez-le dans les propriétés de la pétition
    
        datastore.put(e); // Mettre à jour l'entité avec l'ID de pétition
    
        return e; // Retourne l'entité de la pétition nouvellement ajoutée
    }
    
    
    @ApiMethod(name = "topTenPetitions", httpMethod = HttpMethod.GET)
    public List<Entity> topTenPetitions() {
        Query q = new Query("Petition")
            .addSort("creationDate", Query.SortDirection.DESCENDING)
            .addSort("numberOfSignatures", Query.SortDirection.DESCENDING);
            
    
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        PreparedQuery pq = datastore.prepare(q);
        List<Entity> result = pq.asList(FetchOptions.Builder.withLimit(10));
    
        List<Entity> responseList = new ArrayList<>();
    
        for (Entity petition : result) {
            // Ajouter l'ID de la pétition à la liste de réponses
            Key petitionKey = petition.getKey();
            petition.setProperty("id", petitionKey.getId());
            responseList.add(petition);
        }
    
        return responseList;
    }
    
    
    @ApiMethod(name = "signPetition", httpMethod = HttpMethod.GET)
    public Entity signPetition(@Named("petitionId") String petitionId, @Named("userId") String userId) {
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    
        // Recherche de l'entité de la pétition par son ID
        Query.Filter filter = new Query.FilterPredicate("ID_pet", Query.FilterOperator.EQUAL, Long.parseLong(petitionId));
        Query query = new Query("Petition").setFilter(filter);
        Entity petitionEntity = datastore.prepare(query).asSingleEntity();
    
        if (petitionEntity != null) {
            List<String> signatures = (List<String>) petitionEntity.getProperty("signatures");
    
            if (signatures != null && signatures.contains(userId)) {
                return petitionEntity; // Retourner la pétition si l'utilisateur a déjà signé
            } 
            if (signatures == null) {
                signatures = new ArrayList<>(); // Initialisation de la liste des signatures si elle est null
            }
            signatures.add(userId); // Ajout de l'utilisateur à la liste des signatures
            petitionEntity.setProperty("signatures", signatures);
            petitionEntity.setProperty("numberOfSignatures", signatures.size());
    
            datastore.put(petitionEntity); // Mettre à jour l'entité avec les nouvelles signatures
        }
    
        return petitionEntity;
    }
    
    @ApiMethod(name = "userSignedPetitions", httpMethod = HttpMethod.GET)
    public List<Entity> userSignedPetitions(@Named("userId") String userId) {
        Query.Filter userFilter = new Query.FilterPredicate("signatures", Query.FilterOperator.EQUAL, userId);

        Query q = new Query("Petition").setFilter(userFilter).addSort("creationDate", Query.SortDirection.DESCENDING);

        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        PreparedQuery pq = datastore.prepare(q);
        List<Entity> result = pq.asList(FetchOptions.Builder.withLimit(10));

        List<Entity> responseList = new ArrayList<>();

        for (Entity petition : result) {
            // Ajouter l'ID de la pétition à la liste de réponses
            Key petitionKey = petition.getKey();
            petition.setProperty("id", petitionKey.getId());
            responseList.add(petition);
        }

        return responseList;
}

@ApiMethod(name = "findPetitionsByTag", httpMethod = HttpMethod.GET)
public List<Entity> findPetitionsByTag(@Named("tag") String tag) {
    Query.Filter tagFilter = new Query.FilterPredicate("tags", Query.FilterOperator.EQUAL, tag);

    Query q = new Query("Petition").setFilter(tagFilter).addSort("creationDate", SortDirection.DESCENDING);

    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    PreparedQuery pq = datastore.prepare(q);
    List<Entity> result = pq.asList(FetchOptions.Builder.withLimit(10));

    List<Entity> responseList = new ArrayList<>();

    for (Entity petition : result) {
        // Ajouter l'ID de la pétition à la liste de réponses
        Key petitionKey = petition.getKey();
        petition.setProperty("id", petitionKey.getId());
        responseList.add(petition);
    }

    return responseList;
}


}
