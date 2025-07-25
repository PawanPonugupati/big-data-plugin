/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package org.pentaho.big.data.kettle.plugins.mapreduce.step.exit;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.core.RowSet;
import org.pentaho.di.core.bowl.Bowl;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.logging.LoggingObjectInterface;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.steps.mock.StepMockHelper;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.AdditionalMatchers.aryEq;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by bryan on 1/18/16.
 */
public class HadoopExitTest {
  private StepMockHelper<HadoopExitMeta, HadoopExitData> stepMockHelper;
  private HadoopExit hadoopExit;
  private LogChannelInterface logChannelInterface;

  @Before
  public void setup() {
    stepMockHelper = new StepMockHelper<>( "hadoopExit", HadoopExitMeta.class, HadoopExitData.class );
    when( stepMockHelper.logChannelInterfaceFactory.create( any(), any( LoggingObjectInterface.class ) ) )
      .thenReturn( stepMockHelper.logChannelInterface );
    when( stepMockHelper.trans.isRunning() ).thenReturn( true );
    hadoopExit =
      new HadoopExit( stepMockHelper.stepMeta, stepMockHelper.stepDataInterface, 0, stepMockHelper.transMeta,
        stepMockHelper.trans );
    hadoopExit.init( stepMockHelper.initStepMetaInterface, stepMockHelper.initStepDataInterface );
  }

  @After
  public void teardown() {
    stepMockHelper.cleanUp();
  }

  @Test( timeout = 5000 )
  public void testProcessRow() throws KettleException {
    Object[] row1 = new Object[] { 0, 1 };
    Object[] row2 = new Object[] { 1, 0 };
    when( stepMockHelper.processRowsStepDataInterface.getInValueOrdinal() ).thenReturn( 1 );
    RowSet mockInputRowSet = stepMockHelper.getMockInputRowSet( row1, row2 );
    hadoopExit.addRowSetToInputRowSets( mockInputRowSet );
    RowSet outputRowSet = mock( RowSet.class );
    hadoopExit.addRowSetToOutputRowSets( outputRowSet );
    RowMetaInterface rowMetaInterface = mock( RowMetaInterface.class );
    when( rowMetaInterface.clone() ).thenReturn( rowMetaInterface );
    when( stepMockHelper.processRowsStepDataInterface.getOutputRowMeta() ).thenReturn( rowMetaInterface );
    when( outputRowSet.putRow( eq( rowMetaInterface ), aryEq( row1 ) ) ).thenReturn( true );
    when( outputRowSet.putRow( eq( rowMetaInterface ), aryEq( row2 ) ) ).thenReturn( true );
    assertTrue( hadoopExit
      .processRow( stepMockHelper.processRowsStepMetaInterface, stepMockHelper.processRowsStepDataInterface ) );
    assertTrue( hadoopExit
      .processRow( stepMockHelper.processRowsStepMetaInterface, stepMockHelper.processRowsStepDataInterface ) );
    assertFalse( hadoopExit
      .processRow( stepMockHelper.processRowsStepMetaInterface, stepMockHelper.processRowsStepDataInterface ) );
    verify( stepMockHelper.processRowsStepDataInterface )
      .init( any( Bowl.class), any( RowMetaInterface.class ), eq( stepMockHelper.processRowsStepMetaInterface ),
             eq( hadoopExit ) );
    verify( outputRowSet ).putRow( eq( rowMetaInterface ), aryEq( row1 ) );
    verify( outputRowSet ).putRow( eq( rowMetaInterface ), aryEq( row2 ) );
  }
}
