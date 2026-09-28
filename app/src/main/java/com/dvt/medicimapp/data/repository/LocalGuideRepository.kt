package com.dvt.medicimapp.data.repository

import com.dvt.medicimapp.data.model.GuiaItem

class LocalGuideRepository : GuideRepository {
    override val categorias = listOf("Todas", "Hogar", "Personal", "Comunidad")
    private val guias = listOf(
        GuiaItem("Tapa recipientes con agua", "Cubre baldes, tanques y cualquier recipiente que almacene agua. El mosquito del dengue se reproduce en agua limpia estancada.", "💧", "Hogar"),
        GuiaItem("Elimina agua estancada", "Vacía y limpia floreros, bebederos, platillos de macetas y cualquier objeto que acumule agua al menos cada 3 días.", "🪣", "Hogar"),
        GuiaItem("Limpia canaletas y desagües", "Revisa que canaletas, desagües y techos estén limpios y sin obstrucciones para evitar acumulación de agua.", "🏠", "Hogar"),
        GuiaItem("Guarda llantas bajo techo", "Las llantas acumulan agua de lluvia y son criaderos ideales. Guárdalas cubiertas o perfóralas si no las usas.", "🔧", "Hogar"),
        GuiaItem("Usa repelente de insectos", "Aplica repelente en zonas expuestas de la piel al salir de casa, especialmente en horas de mayor actividad del mosquito (amanecer y atardecer).", "🧴", "Personal"),
        GuiaItem("Usa ropa protectora", "Viste ropa de manga larga y pantalones largos, especialmente en zonas boscosas o con alta presencia de mosquitos.", "👕", "Personal"),
        GuiaItem("Usa mosquiteros", "Coloca mosquiteros en ventanas, puertas y camas. Son especialmente importantes para proteger a niños y personas mayores.", "🪟", "Personal"),
        GuiaItem("Consulta al médico ante síntomas", "Si presentas fiebre alta, dolor de cabeza intenso, dolor detrás de los ojos o sarpullido, acude inmediatamente a un centro de salud.", "🩺", "Personal"),
        GuiaItem("Reporta criaderos en tu zona", "Si encuentras focos de mosquitos o criaderos en espacios públicos, repórtalos a las autoridades de salud o usa esta app.", "📢", "Comunidad"),
        GuiaItem("Participa en jornadas de fumigación", "Colabora con las campañas de fumigación organizadas por el MINSA y la municipalidad en tu distrito.", "🏘️", "Comunidad"),
        GuiaItem("Informa a tus vecinos", "Comparte información sobre prevención del dengue con familiares y vecinos. La prevención comunitaria es clave para reducir casos.", "👥", "Comunidad"),
        GuiaItem("Mantén limpio el espacio público", "Participa en actividades de limpieza de parques, calles y áreas comunales para eliminar posibles criaderos.", "🌿", "Comunidad"),
    )

    override fun obtenerGuias(): List<GuiaItem> = guias
}
