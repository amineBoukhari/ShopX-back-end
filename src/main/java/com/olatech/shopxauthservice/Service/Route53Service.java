package com.olatech.shopxauthservice.Service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.route53.Route53Client;
import software.amazon.awssdk.services.route53.model.*;

import java.util.List;

@Service
public class Route53Service {
    private static final Logger logger = LoggerFactory.getLogger(Route53Service.class);

    private final Route53Client route53Client;
    private final String hostedZoneId;
    private final String rootDomain;
    private final String nuxtServerIp;

    public Route53Service(
            @Value("${aws.access.key.id}") String accessKey,
            @Value("${aws.secret.access.key}") String secretKey,
            @Value("${aws.route53.hosted.zone.id}") String hostedZoneId,
            @Value("${aws.route53.root.domain}") String rootDomain,
            @Value("${shopx.nuxt.server.ip}") String nuxtServerIp) {
        
        AwsBasicCredentials awsCredentials = AwsBasicCredentials.create(accessKey, secretKey);
        this.route53Client = Route53Client.builder()
                .region(Region.AWS_GLOBAL)
                .credentialsProvider(StaticCredentialsProvider.create(awsCredentials))
                .build();
        
        this.hostedZoneId = hostedZoneId;
        this.rootDomain = rootDomain;
        this.nuxtServerIp = nuxtServerIp;
        
        logger.info("Route53Service initialized with root domain: {}", rootDomain);
    }

    /**
     * Crée ou met à jour un enregistrement A pour un sous-domaine
     *
     * @param subdomain Le nom du sous-domaine (sans le nom de domaine racine)
     * @return true si l'opération a réussi
     */
    public boolean createSubdomainRecord(String subdomain) {
        try {
            String fullyQualifiedDomain = subdomain + "." + rootDomain;
            logger.info("Creating subdomain record for: {}", fullyQualifiedDomain);
            
            ResourceRecord resourceRecord = ResourceRecord.builder()
                    .value(nuxtServerIp)
                    .build();
            
            ResourceRecordSet recordSet = ResourceRecordSet.builder()
                    .name(fullyQualifiedDomain)
                    .type(RRType.A)
                    .ttl(300L)
                    .resourceRecords(resourceRecord)
                    .build();
            
            Change change = Change.builder()
                    .action(ChangeAction.UPSERT)
                    .resourceRecordSet(recordSet)
                    .build();
            
            ChangeBatch changeBatch = ChangeBatch.builder()
                    .changes(change)
                    .build();
            
            ChangeResourceRecordSetsRequest request = ChangeResourceRecordSetsRequest.builder()
                    .hostedZoneId(hostedZoneId)
                    .changeBatch(changeBatch)
                    .build();
            
            ChangeResourceRecordSetsResponse response = route53Client.changeResourceRecordSets(request);
            logger.info("Route53 change request submitted with status: {}", response.changeInfo().status());
            
            return true;
        } catch (Exception e) {
            logger.error("Error creating subdomain record: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * Supprime un enregistrement de sous-domaine
     *
     * @param subdomain Le nom du sous-domaine (sans le nom de domaine racine)
     * @return true si l'opération a réussi
     */
    public boolean deleteSubdomainRecord(String subdomain) {
        try {
            String fullyQualifiedDomain = subdomain + "." + rootDomain;
            logger.info("Deleting subdomain record for: {}", fullyQualifiedDomain);
            
            // On doit d'abord récupérer l'enregistrement existant
            ListResourceRecordSetsRequest listRequest = ListResourceRecordSetsRequest.builder()
                    .hostedZoneId(hostedZoneId)
                    .startRecordName(fullyQualifiedDomain)
                    .startRecordType(RRType.A)
                    .maxItems("1")
                    .build();
            
            ListResourceRecordSetsResponse result = route53Client.listResourceRecordSets(listRequest);
            
            if (result.resourceRecordSets().isEmpty()) {
                logger.warn("No record found for subdomain: {}", fullyQualifiedDomain);
                return true; // L'enregistrement n'existe pas, donc la suppression est un succès
            }
            
            // On vérifie que c'est bien l'enregistrement que nous cherchons
            ResourceRecordSet existingRecordSet = result.resourceRecordSets().get(0);
            if (!existingRecordSet.name().equalsIgnoreCase(fullyQualifiedDomain + ".")) {
                logger.warn("Found record doesn't match requested subdomain. Found: {}, Requested: {}",
                        existingRecordSet.name(), fullyQualifiedDomain);
                return false;
            }
            
            // Créer une demande de changement pour supprimer l'enregistrement
            Change change = Change.builder()
                    .action(ChangeAction.DELETE)
                    .resourceRecordSet(existingRecordSet)
                    .build();
            
            ChangeBatch changeBatch = ChangeBatch.builder()
                    .changes(change)
                    .build();
            
            ChangeResourceRecordSetsRequest request = ChangeResourceRecordSetsRequest.builder()
                    .hostedZoneId(hostedZoneId)
                    .changeBatch(changeBatch)
                    .build();
            
            ChangeResourceRecordSetsResponse response = route53Client.changeResourceRecordSets(request);
            logger.info("Route53 delete request submitted with status: {}", response.changeInfo().status());
            
            return true;
        } catch (Exception e) {
            logger.error("Error deleting subdomain record: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * Vérifie si un sous-domaine existe déjà dans Route53
     *
     * @param subdomain Le nom du sous-domaine à vérifier
     * @return true si le sous-domaine existe déjà
     */
    public boolean subdomainExists(String subdomain) {
        try {
            String fullyQualifiedDomain = subdomain + "." + rootDomain;
            
            ListResourceRecordSetsRequest listRequest = ListResourceRecordSetsRequest.builder()
                    .hostedZoneId(hostedZoneId)
                    .startRecordName(fullyQualifiedDomain)
                    .startRecordType(RRType.A)
                    .maxItems("1")
                    .build();
            
            ListResourceRecordSetsResponse result = route53Client.listResourceRecordSets(listRequest);
            
            if (result.resourceRecordSets().isEmpty()) {
                return false;
            }
            
            ResourceRecordSet recordSet = result.resourceRecordSets().get(0);
            // Route53 ajoute un point à la fin du nom de domaine
            return recordSet.name().equalsIgnoreCase(fullyQualifiedDomain + ".");
            
        } catch (Exception e) {
            logger.error("Error checking subdomain existence: {}", e.getMessage(), e);
            return false;
        }
    }
}
