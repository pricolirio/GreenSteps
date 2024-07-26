package com.example.greensteps

import Model.DataManagerStatistics
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import junit.framework.TestCase.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations


class DataManagerUserTest {

    @Mock
    private lateinit var mockAuth: FirebaseAuth

    @Mock
    private lateinit var mockDb: FirebaseFirestore

    private lateinit var dataManager: DataManagerStatistics


    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)

        dataManager = DataManagerStatistics(mockAuth, mockDb)
    }

    @Test
    fun testCalcularEdad() {
        val edad = dataManager.calcularEdad("01/01/2000")
        assertEquals(24, edad)
    }

    @Test
    fun testObtenerRangoEdad() {
        val rangoEdad = dataManager.obtenerRangoEdad(30)
        assertEquals("de 25 a 35", rangoEdad)
    }

    @Test
    fun testObtenerRangoEdadMenorDe25() {
        val rangoEdad = dataManager.obtenerRangoEdad(20)
        assertEquals("menor de 25", rangoEdad)
    }

    @Test
    fun testObtenerRangoEdadMayorDe45() {
        val rangoEdad = dataManager.obtenerRangoEdad(50)
        assertEquals("mayor de 45", rangoEdad)
    }




}
