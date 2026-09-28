package com.dvt.medicimapp.data.repository

import com.dvt.medicimapp.data.model.PreguntaMoskiCheck

internal val preguntasMoskiCheck = listOf(

    // 11 síntomas utilizados por el modelo
    PreguntaMoskiCheck(
        campo = "sudden_fever",
        texto = "¿Tiene fiebre repentina?"
    ),
    PreguntaMoskiCheck(
        campo = "headache",
        texto = "¿Tiene dolor de cabeza?"
    ),
    PreguntaMoskiCheck(
        campo = "muscle_pain",
        texto = "¿Tiene dolor muscular?"
    ),
    PreguntaMoskiCheck(
        campo = "joint_pain",
        texto = "¿Tiene dolor articular?"
    ),
    PreguntaMoskiCheck(
        campo = "vomiting",
        texto = "¿Ha presentado vómitos?"
    ),
    PreguntaMoskiCheck(
        campo = "rash",
        texto = "¿Tiene sarpullido o erupciones en la piel?"
    ),
    PreguntaMoskiCheck(
        campo = "nausea",
        texto = "¿Tiene náuseas?"
    ),
    PreguntaMoskiCheck(
        campo = "fatigue",
        texto = "¿Siente cansancio o fatiga?"
    ),
    PreguntaMoskiCheck(
        campo = "orbital_pain",
        texto = "¿Tiene dolor detrás de los ojos?"
    ),
    PreguntaMoskiCheck(
        campo = "red_eyes",
        texto = "¿Tiene los ojos rojos?"
    ),
    PreguntaMoskiCheck(
        campo = "swelling",
        texto = "¿Presenta hinchazón?"
    ),

    // 3 señales de seguridad
    PreguntaMoskiCheck(
        campo = "dolor_abdominal_intenso",
        texto = "¿Presenta dolor abdominal intenso o persistente?",
        esSeguridad = true
    ),
    PreguntaMoskiCheck(
        campo = "sangrado",
        texto = "¿Ha presentado sangrado inusual, como sangrado de nariz o encías?",
        esSeguridad = true
    ),
    PreguntaMoskiCheck(
        campo = "vomitos_persistentes",
        texto = "¿Ha presentado vómitos persistentes o muy frecuentes?",
        esSeguridad = true
    )
)
