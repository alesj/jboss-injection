/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat Middleware LLC, and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.injection.resolve.test.unit;

import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.injection.resolve.naming.EnvironmentProcessor;
import org.jboss.injection.resolve.naming.ReferenceResolverResult;
import org.jboss.injection.resolve.naming.ResolutionException;
import org.jboss.injection.resolve.spi.DuplicateReferenceValidator;
import org.jboss.injection.resolve.spi.EnvironmentMetaDataVisitor;
import org.jboss.injection.resolve.spi.Resolver;
import org.jboss.injection.resolve.spi.ResolverResult;
import org.jboss.metadata.javaee.spec.EJBReferenceMetaData;
import org.jboss.metadata.javaee.spec.EJBReferencesMetaData;
import org.jboss.metadata.javaee.spec.Environment;
import org.jboss.metadata.javaee.spec.EnvironmentEntriesMetaData;
import org.jboss.metadata.javaee.spec.EnvironmentEntryMetaData;
import org.jboss.metadata.javaee.spec.ResourceAuthorityType;
import org.jboss.metadata.javaee.spec.ResourceReferenceMetaData;
import org.jboss.metadata.javaee.spec.ResourceReferencesMetaData;
import org.jboss.metadata.javaee.spec.ResourceSharingScopeType;
import org.junit.Assert;
import org.junit.Test;

import javax.naming.LinkRef;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test to ensure the functionality of the EnvironmentProcessor
 *
 * @author <a href="mailto:jbailey@redhat.com">John Bailey</a>
 */
public class EnvironmentProcessorTest
{

   @Test
   public void testResolverMatching() throws Exception
   {
      EJBReferencesMetaData referencesMetaData = new EJBReferencesMetaData();

      EJBReferenceMetaData referenceMetaData = new EJBReferenceMetaData();
      referenceMetaData.setEjbRefName("testRef");

      referencesMetaData.add(referenceMetaData);

      Environment environment = mock(Environment.class);
      when(environment.getEjbReferences()).thenReturn(referencesMetaData);

      EnvironmentProcessor<DeploymentUnit> processor = new EnvironmentProcessor<DeploymentUnit>();
      DeploymentUnit unit = mock(DeploymentUnit.class);

      processor.addMetaDataVisitor(new EnvironmentMetaDataVisitor<EJBReferenceMetaData>()
      {
         public Iterable<EJBReferenceMetaData> getMetaData(final Environment environment)
         {
            return environment.getEjbReferences();
         }

         public Class<EJBReferenceMetaData> getMetaDataType()
         {
            return EJBReferenceMetaData.class;
         }
      });

      try
      {
         processor.process(unit, environment);
         Assert.fail("Should throw exception if no Resolver can be found");
      }
      catch(ResolutionException expected)
      {
      }

      processor.addResolver(new Resolver<EJBReferenceMetaData, DeploymentUnit, ReferenceResolverResult>()
      {
         public Class<EJBReferenceMetaData> getMetaDataType()
         {
            return EJBReferenceMetaData.class;
         }

         public ReferenceResolverResult resolve(final DeploymentUnit context, final EJBReferenceMetaData metaData)
         {
            return new ReferenceResolverResult("org.jboss.test.Bean.test", "testBean", "java:testBean");
         }
      });

      List<ResolverResult<?>> results = processor.process(unit, environment);
      Assert.assertNotNull(results);
      Assert.assertEquals(1, results.size());
      ResolverResult result = results.get(0);
      Assert.assertEquals("org.jboss.test.Bean.test", result.getRefName());
      Assert.assertEquals("testBean", result.getBeanName());
      Assert.assertEquals("java:testBean", ((LinkRef)result.getValue()).getLinkName());
   }


   @Test
   public void testResolverWithNonConflictingEnvironmentEntries() throws Exception
   {
      EnvironmentEntriesMetaData entriesMetaData = new EnvironmentEntriesMetaData();
      EnvironmentEntryMetaData entryMetaData = new EnvironmentEntryMetaData();
      entryMetaData.setType(String.class.getName());
      entryMetaData.setValue("value");
      entryMetaData.setEnvEntryName("test");
      entriesMetaData.add(entryMetaData);

      Environment environmentOne = mock(Environment.class);
      when(environmentOne.getEnvironmentEntries()).thenReturn(entriesMetaData);
      Environment environmentTwo = mock(Environment.class);
      when(environmentTwo.getEnvironmentEntries()).thenReturn(entriesMetaData);

      EnvironmentProcessor<DeploymentUnit> processor = new EnvironmentProcessor<DeploymentUnit>();
      DeploymentUnit unit = mock(DeploymentUnit.class);

      processor.addMetaDataVisitor(new EnvironmentMetaDataVisitor<EnvironmentEntryMetaData>()
      {
         public Iterable<EnvironmentEntryMetaData> getMetaData(final Environment environment)
         {
            return environment.getEnvironmentEntries();
         }

         public Class<EnvironmentEntryMetaData> getMetaDataType()
         {
            return EnvironmentEntryMetaData.class;
         }
      });

      processor.addResolver(new Resolver<EnvironmentEntryMetaData, DeploymentUnit, ResolverResult<String>>()
      {
         public Class<EnvironmentEntryMetaData> getMetaDataType()
         {
            return EnvironmentEntryMetaData.class;
         }

         public ResolverResult<String> resolve(final DeploymentUnit context, final EnvironmentEntryMetaData metaData)
         {
            return new ResolverResult<String>("java:comp/env/test", "testBean", metaData.getValue());
         }
      });


      List<ResolverResult<?>> result = processor.process(unit, environmentOne, environmentTwo);
      Assert.assertEquals(1, result.size());
      Assert.assertEquals("value", result.get(0).getValue());
   }

   @Test
   public void testResolverWithConflictingEnvironmentEntries() throws Exception
   {
      EnvironmentEntriesMetaData entriesMetaData = new EnvironmentEntriesMetaData();
      EnvironmentEntryMetaData entryMetaData = new EnvironmentEntryMetaData();
      entryMetaData.setType(String.class.getName());
      entryMetaData.setValue("value");
      entryMetaData.setEnvEntryName("test");
      entriesMetaData.add(entryMetaData);

      Environment environmentOne = mock(Environment.class);
      when(environmentOne.getEnvironmentEntries()).thenReturn(entriesMetaData);

      entriesMetaData = new EnvironmentEntriesMetaData();
      entryMetaData = new EnvironmentEntryMetaData();
      entryMetaData.setType(String.class.getName());
      entryMetaData.setValue("other value");
      entryMetaData.setEnvEntryName("test");
      entriesMetaData.add(entryMetaData);

      Environment environmentTwo = mock(Environment.class);
      when(environmentTwo.getEnvironmentEntries()).thenReturn(entriesMetaData);

      EnvironmentProcessor<DeploymentUnit> processor = new EnvironmentProcessor<DeploymentUnit>();
      DeploymentUnit unit = mock(DeploymentUnit.class);

      processor.addMetaDataVisitor(new EnvironmentMetaDataVisitor<EnvironmentEntryMetaData>()
      {
         public Iterable<EnvironmentEntryMetaData> getMetaData(final Environment environment)
         {
            return environment.getEnvironmentEntries();
         }

         public Class<EnvironmentEntryMetaData> getMetaDataType()
         {
            return EnvironmentEntryMetaData.class;
         }
      });

      processor.addResolver(new Resolver<EnvironmentEntryMetaData, DeploymentUnit, ResolverResult<String>>()
      {
         public Class<EnvironmentEntryMetaData> getMetaDataType()
         {
            return EnvironmentEntryMetaData.class;
         }

         public ResolverResult<String> resolve(final DeploymentUnit context, final EnvironmentEntryMetaData metaData)
         {
            return new ResolverResult<String>("java:comp/env/test", "testBean", metaData.getValue());
         }
      });

      try
      {
         processor.process(unit, environmentOne, environmentTwo);
         Assert.fail("Should have thrown ResolutionException based on conflicting references");
      }
      catch(ResolutionException expected)
      {
      }
   }

   @Test
   public void testCustomReferenceValidatorNonConflicting() throws Exception
   {
      ResourceReferencesMetaData referencesMetaData = new ResourceReferencesMetaData();
      ResourceReferenceMetaData referenceMetaData = new ResourceReferenceMetaData();
      referenceMetaData.setType(String.class.getName());
      referenceMetaData.setResAuth(ResourceAuthorityType.Application);
      referenceMetaData.setResSharingScope(ResourceSharingScopeType.Shareable);
      referenceMetaData.setResourceRefName("java:test");
      referencesMetaData.add(referenceMetaData);

      Environment environmentOne = mock(Environment.class);
      when(environmentOne.getResourceReferences()).thenReturn(referencesMetaData);
      Environment environmentTwo = mock(Environment.class);
      when(environmentTwo.getResourceReferences()).thenReturn(referencesMetaData);

      EnvironmentProcessor<DeploymentUnit> processor = new EnvironmentProcessor<DeploymentUnit>();
      DeploymentUnit unit = mock(DeploymentUnit.class);

      processor.addMetaDataVisitor(new EnvironmentMetaDataVisitor<ResourceReferenceMetaData>()
      {
         public Iterable<ResourceReferenceMetaData> getMetaData(final Environment environment)
         {
            return environment.getResourceReferences();
         }

         public Class<ResourceReferenceMetaData> getMetaDataType()
         {
            return ResourceReferenceMetaData.class;
         }
      });

      processor.addResolver(new Resolver<ResourceReferenceMetaData, DeploymentUnit, ResolverResult<String>>()
      {
         public Class<ResourceReferenceMetaData> getMetaDataType()
         {
            return ResourceReferenceMetaData.class;
         }

         public ResolverResult<String> resolve(final DeploymentUnit context, final ResourceReferenceMetaData metaData)
         {
            return new ResolverResult<String>("java:comp/env/test", "testBean", metaData.getResourceRefName());
         }
      });

      processor.addDuplicateReferenceValidator(new DuplicateReferenceValidator<ResourceReferenceMetaData>(ResourceReferenceMetaData.class) {
         @Override
         protected boolean attributesEqual(final ResourceReferenceMetaData previousReference, final ResourceReferenceMetaData newReference)
         {
            return previousReference.getResAuth().equals(newReference.getResAuth()) && previousReference.getResSharingScope().equals(newReference.getResSharingScope());
         }
      });

      List<ResolverResult<?>> result = processor.process(unit, environmentOne, environmentTwo);
      Assert.assertEquals(1, result.size());
      Assert.assertEquals("java:test", result.get(0).getValue());
   }

   @Test
   public void testCustomReferenceValidatorConflicting() throws Exception
   {

      ResourceReferencesMetaData referencesMetaData = new ResourceReferencesMetaData();
      ResourceReferenceMetaData referenceMetaData = new ResourceReferenceMetaData();
      referenceMetaData.setType(String.class.getName());
      referenceMetaData.setResAuth(ResourceAuthorityType.Application);
      referenceMetaData.setResSharingScope(ResourceSharingScopeType.Shareable);
      referenceMetaData.setResourceRefName("java:test");
      referencesMetaData.add(referenceMetaData);

      Environment environmentOne = mock(Environment.class);
      when(environmentOne.getResourceReferences()).thenReturn(referencesMetaData);


      referencesMetaData = new ResourceReferencesMetaData();
      referenceMetaData = new ResourceReferenceMetaData();
      referenceMetaData.setType(String.class.getName());
      referenceMetaData.setResAuth(ResourceAuthorityType.Application);
      referenceMetaData.setResSharingScope(ResourceSharingScopeType.Unshareable);
      referenceMetaData.setResourceRefName("java:test");
      referencesMetaData.add(referenceMetaData);

      Environment environmentTwo = mock(Environment.class);
      when(environmentTwo.getResourceReferences()).thenReturn(referencesMetaData);

      EnvironmentProcessor<DeploymentUnit> processor = new EnvironmentProcessor<DeploymentUnit>();
      DeploymentUnit unit = mock(DeploymentUnit.class);

      processor.addMetaDataVisitor(new EnvironmentMetaDataVisitor<ResourceReferenceMetaData>()
      {
         public Iterable<ResourceReferenceMetaData> getMetaData(final Environment environment)
         {
            return environment.getResourceReferences();
         }

         public Class<ResourceReferenceMetaData> getMetaDataType()
         {
            return ResourceReferenceMetaData.class;
         }
      });

      processor.addResolver(new Resolver<ResourceReferenceMetaData, DeploymentUnit, ResolverResult<String>>()
      {
         public Class<ResourceReferenceMetaData> getMetaDataType()
         {
            return ResourceReferenceMetaData.class;
         }

         public ResolverResult<String> resolve(final DeploymentUnit context, final ResourceReferenceMetaData metaData)
         {
            return new ResolverResult<String>("java:comp/env/test", "testBean", metaData.getResourceRefName());
         }
      });

      processor.addDuplicateReferenceValidator(new DuplicateReferenceValidator<ResourceReferenceMetaData>(ResourceReferenceMetaData.class) {
         @Override
         protected boolean attributesEqual(final ResourceReferenceMetaData previousReference, final ResourceReferenceMetaData newReference)
         {
            return previousReference.getResAuth().equals(newReference.getResAuth()) && previousReference.getResSharingScope().equals(newReference.getResSharingScope());
         }
      });

      try
      {
         processor.process(unit, environmentOne, environmentTwo);
         Assert.fail("Should have thrown ResolutionException based on conflicting references");
      }
      catch(ResolutionException expected)
      {
      }
   }

}
