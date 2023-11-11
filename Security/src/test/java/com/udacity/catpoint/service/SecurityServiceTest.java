package com.udacity.catpoint.service;

import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.udacity.catpoint.data.*;
import com.udacity.catpoint.image.service.FakeImageService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import java.awt.image.BufferedImage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SecurityServiceTest {

    @Mock
    private FakeImageService imageServiceTest;

    @Mock
    private SecurityRepository securityRepository;
    private SecurityService securityService;
    // Add sensor for detecting
    Sensor sensor_window = new Sensor("Window", SensorType.WINDOW);
    Sensor sensor_motion = new Sensor("Motion", SensorType.MOTION);
    Sensor sensor_door = new Sensor("Door", SensorType.DOOR);
    // init bufferImage
    BufferedImage img = new BufferedImage(256, 256, BufferedImage.TYPE_INT_RGB);

    @BeforeEach
	void init() {
        // Create instance for SecurityService
		securityService = new SecurityService(securityRepository, imageServiceTest);

        // Add Reprository
        securityRepository.addSensor(sensor_window);
        securityRepository.addSensor(sensor_motion);
        securityRepository.addSensor(sensor_door);

	}

    // Check below test on the web

    //Test3.  nnnnn xxxx
    @ParameterizedTest
    @EnumSource(value = ArmingStatus.class, names = {"ARMED_AWAY","ARMED_HOME"})
    @DisplayName("Test 3")
   public void alarmPending_and_allSensorInactivate_setAlarmStatusToNoAlarm(ArmingStatus armingStatus) {

        // check status
        when(securityRepository.getArmingStatus()).thenReturn(armingStatus);
        // check status
        when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.PENDING_ALARM);


        // do when active = true
        securityService.changeSensorActivationStatus(sensor_door, true);
        // do when active = false
        securityService.changeSensorActivationStatus(sensor_door, false);
        // verify the status
        verify(securityRepository).setAlarmStatus(AlarmStatus.NO_ALARM);
   }

    //Test4. 
    @ParameterizedTest
    @DisplayName("Test 4")
    @ValueSource(booleans = {true, false})
    public void alarmActive_and_changeSensorState_doesNotAffectAlarmState(boolean sensorStatus) {
        // check status of alarm
         when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.ALARM);
         // do with sensor
         securityService.changeSensorActivationStatus(sensor_door, sensorStatus);
        // verify the status
        verify(securityRepository,never()).setAlarmStatus(any(AlarmStatus.class));
    }

    //Test5. 
    @Test
    @DisplayName("Test 5")
    public void sensorActivated_whileAlreadyActive_and_systemPending_changeToAlarmState() {
        // set status for the sensor dooor
        sensor_door.setActive(true);
        // check status
        when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.PENDING_ALARM);
        // do with sensor
        securityService.changeSensorActivationStatus(sensor_door, true);
        // verify the status
        verify(securityRepository).setAlarmStatus(AlarmStatus.ALARM);
    }


    //Test9. 
    @Test
    @DisplayName("Test 9")
    public void systemDisarmed_setStatusNoAlarm() {
        // do with sensor
        securityService.setArmingStatus(ArmingStatus.DISARMED);
        // verify the status
        verify(securityRepository).setAlarmStatus(AlarmStatus.NO_ALARM);
    }

    //Test10. 
    @ParameterizedTest
    @EnumSource(value = ArmingStatus.class, names = {"ARMED_AWAY","ARMED_HOME"})
    @DisplayName("Test 10")
    public void systemArmed_resetAllSensorsInactive(ArmingStatus armingStatus) {
        // check status
        when(securityRepository.getArmingStatus()).thenReturn(ArmingStatus.DISARMED);
        // do with status with avtive true
        securityService.changeSensorActivationStatus(sensor_door, true);
        securityService.changeSensorActivationStatus(sensor_window, true);
        securityService.changeSensorActivationStatus(sensor_motion, true);
        securityService.setArmingStatus(armingStatus);
        // verify the status
        assertEquals(false,securityService.areSensorsActive());
    }
    //Test11.nnnn
    @Test
    @DisplayName("Tests Requirement #11")
    public void systemArmedHome_and_whileCameraShowsACat_setAlarmToAlarm() {

        // check status
        when(securityRepository.getArmingStatus()).thenReturn(ArmingStatus.DISARMED);
        // check status
        when(imageServiceTest.imageContainsCat(any(), ArgumentMatchers.anyFloat())).thenReturn(true);

        // process processImage
        securityService.processImage(img);
        // change status after processing
        securityService.setArmingStatus(ArmingStatus.ARMED_HOME);
        // verify the status
        verify(securityRepository).setAlarmStatus(AlarmStatus.ALARM);
    }

    // this test 12 but fill 3 dont know
    //Test12. 
    @Test
    @DisplayName("Tests Requirement #3")
    public void systemDisarmed_and_alarmed_setAlarmStatusToPendingAlarm() {
        // check status
        when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.ALARM);
        // check status
        when(securityRepository.getArmingStatus()).thenReturn(ArmingStatus.DISARMED);
        // process with the active falses
        securityService.changeSensorActivationStatus(sensor_door, false);
        verify(securityRepository).setAlarmStatus(AlarmStatus.PENDING_ALARM);
    }

    //Test6.
    @ParameterizedTest
    @DisplayName("Test 6")
    @EnumSource(AlarmStatus.class)
    public void sensorActivated_whileAlreadyActive_and_systemPending_changeToAlarmState(AlarmStatus alarmStatus) {
        // set status
        sensor_door.setActive(false);
        // check status
        when(securityRepository.getAlarmStatus()).thenReturn(alarmStatus);
        // do with sensor
        securityService.changeSensorActivationStatus(sensor_door, false);
        // verify the status
        verify(securityRepository,never()).setAlarmStatus(any(AlarmStatus.class));
    }

    //Test7. nnnnn
    @Test
    @DisplayName("Test 7")
    public void identifiesAnImageACat_whileSystemArmedHome_putSystemIntoAlarm() {
        // check status
        when(securityRepository.getArmingStatus()).thenReturn(ArmingStatus.ARMED_HOME);

        // check status
        when(imageServiceTest.imageContainsCat(any(), ArgumentMatchers.anyFloat())).thenReturn(true);

        // do with sensor
        securityService.processImage(img);
        // verify the status
        verify(securityRepository).setAlarmStatus(AlarmStatus.ALARM);
    }

    //Test8.
    @Test
    @DisplayName("Test 8")
    public void identifiesAnImageACat_doesNotContainACat_changeStatusToNoAlarm_ifSensorsAreNotActive() {
        // check status
        when(imageServiceTest.imageContainsCat(any(), ArgumentMatchers.anyFloat())).thenReturn(false);
        // processImage for process
        securityService.processImage(img);
        // verify the status
        verify(securityRepository).setAlarmStatus(AlarmStatus.NO_ALARM);
    }
    //Test1. nnnnnn
    @ParameterizedTest
    @EnumSource(value = ArmingStatus.class, names = {"ARMED_AWAY","ARMED_HOME"})
    @DisplayName("Test 1")
    public void alarmArmed_and_sensorActivated_pendingAlarmStatus(ArmingStatus armingStatus) {

        // check status
        when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.NO_ALARM);
        // check status
        when(securityRepository.getArmingStatus()).thenReturn(armingStatus);
        // do when active = true
        securityService.changeSensorActivationStatus(sensor_door, true);
        verify(securityRepository).setAlarmStatus(AlarmStatus.PENDING_ALARM);
    }

    //Test2. nnnn
    @ParameterizedTest
    @EnumSource(value = ArmingStatus.class, names = {"ARMED_AWAY","ARMED_HOME"})
    @DisplayName("Test 2")
    public void alarmArmed_and_sensorActivated_and_systemIsPendingAlarmStatus_setAlarmStatusToAlarm(ArmingStatus armingStatus) {

        // check status
        when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.PENDING_ALARM);
        // check status
        when(securityRepository.getArmingStatus()).thenReturn(armingStatus);
        // do when active = true
        securityService.changeSensorActivationStatus(sensor_door, true);
        // verify the status
        verify(securityRepository).setAlarmStatus(AlarmStatus.ALARM);
    }
}