package com.kryptnostic;

import java.net.InetAddress;
import java.util.List;

import org.junit.Test;

public class Ec2SeedProviderTest {

    @Test
    public void testGetSeeds() throws Exception {
        Ec2SeedProvider provider = new Ec2SeedProvider( null );
        List<InetAddress> seeds = provider.getSeeds();
        for ( InetAddress address : seeds ) {
            System.out.println( address.getHostAddress() );
        }
    }
}
