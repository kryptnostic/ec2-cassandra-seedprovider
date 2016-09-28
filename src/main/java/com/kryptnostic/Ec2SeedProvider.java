package com.kryptnostic;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.cassandra.locator.SeedProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.services.ec2.AmazonEC2Async;
import com.amazonaws.services.ec2.AmazonEC2AsyncClientBuilder;
import com.amazonaws.services.ec2.model.DescribeInstancesRequest;
import com.amazonaws.services.ec2.model.DescribeInstancesResult;
import com.amazonaws.services.ec2.model.Filter;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.Reservation;

public class Ec2SeedProvider implements SeedProvider {
    private static final Logger logger = LoggerFactory.getLogger( Ec2SeedProvider.class );

    private static final String REGION = "us-west-2";
    private static AmazonEC2Async ec2;

    public Ec2SeedProvider( Map<String, String> parameters ) {
        ec2 = AmazonEC2AsyncClientBuilder.standard()
                .withRegion( REGION )
                .build();
    }

    @Override
    public List<InetAddress> getSeeds() {
        Filter tagValue = new Filter()
                .withName( "tag-value" )
                .withValues( "seed" );
        Filter tagKey = new Filter()
                .withName( "tag-key" )
                .withValues( "cassandra-node-type" );
        DescribeInstancesRequest req = new DescribeInstancesRequest().withFilters( tagKey, tagValue );

        DescribeInstancesResult describeInstances = ec2.describeInstances( req );

        List<Reservation> reservations = describeInstances.getReservations();
        ArrayList<InetAddress> addresses = new ArrayList<>();
        for ( Reservation res : reservations ) {
            for ( Instance instance : res.getInstances() ) {
                try {
                    if ( instance.getState().getCode() < 17 ) {
                        addresses.add( InetAddress.getByName( instance.getPrivateIpAddress() ) );
                    }
                } catch ( UnknownHostException e ) {
                    logger.error( "Couldn't identify host {}", instance.getPrivateIpAddress(), e );
                }
            }
        }

        return addresses;
    }

}
