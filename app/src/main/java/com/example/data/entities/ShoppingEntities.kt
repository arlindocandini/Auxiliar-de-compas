package com.example.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class AisleCategory(val displayName: String, val icon: String, val colorHex: Long) {
    HORTIFRUTI("Hortifruti / Sacolão", "🍎", 0xFFEBF7EE), // Light Green
    ACOUGUE("Açougue & Carnes", "🥩", 0xFFFCEFF1), // Light Red
    LATICINIOS("Frios & Laticínios", "🧀", 0xFFFFFBEA), // Light Yellow
    PADARIA("Padaria & Massas", "🍞", 0xFFFFF5EB), // Light Orange
    BEBIDAS("Bebidas", "🥤", 0xFFEDF8FD), // Light Blue
    MERCEARIA("Despensa & Grãos", "🧂", 0xFFF8EEFC), // Light Purple
    LIMPEZA("Limpeza", "🧼", 0xFFEDFDFB), // Light Teal
    HIGIENE("Higiene & Perfumaria", "🧴", 0xFFF0F2F3), // Light Grey
    CONGELADOS("Congelados", "❄️", 0xFFEEFAFC), // Light Cyan
    OUTROS("Outros", "📦", 0xFFFAFAFA) // Soft Off-White
}

object Categorizer {
    fun autoCategorize(name: String): AisleCategory {
        val n = name.lowercase().trim()
        return when {
            n.contains("banana") || n.contains("maçã") || n.contains("maca") || n.contains("laranja") || 
            n.contains("uva") || n.contains("morango") || n.contains("limão") || n.contains("limao") ||
            n.contains("abacaxi") || n.contains("melancia") || n.contains("mamão") || n.contains("mamao") ||
            n.contains("alface") || n.contains("tomate") || n.contains("cebola") || n.contains("alho") ||
            n.contains("batata") || n.contains("cenoura") || n.contains("abobrinha") || n.contains("brócolis") ||
            n.contains("brocolis") || n.contains("couve") || n.contains("fruta") || n.contains("legume") ||
            n.contains("verdura") || n.contains("pêra") || n.contains("pera") || n.contains("mamão") || 
            n.contains("manga") || n.contains("maracujá") || n.contains("maracuja") || n.contains("melão") ||
            n.contains("melao") || n.contains("abóbora") || n.contains("abobora") || n.contains("beterraba") ||
            n.contains("pimentão") || n.contains("pimentao") || n.contains("repolho") || n.contains("espinafre") ||
            n.contains("salsa") || n.contains("cebolinha") || n.contains("coentro") || n.contains("cominho") ||
            n.contains("folha") || n.contains("manjericão") || n.contains("manjericao") || n.contains("gengibre") -> AisleCategory.HORTIFRUTI

            n.contains("carne") || n.contains("bife") || n.contains("frango") || n.contains("peixe") ||
            n.contains("porco") || n.contains("linguiça") || n.contains("linguica") || n.contains("salsicha") ||
            n.contains("presunto") || n.contains("salame") || n.contains("patinho") || n.contains("maminha") ||
            n.contains("alcatra") || n.contains("costela") || n.contains("bacon") || n.contains("lombo") ||
            n.contains("pernil") || n.contains("peito") || n.contains("coxa") || n.contains("sobrecoxa") ||
            n.contains("asa") || n.contains("coração") || n.contains("coracao") || n.contains("moela") ||
            n.contains("churrasco") || n.contains("picanha") || n.contains("cupim") || n.contains("contra filé") ||
            n.contains("contra file") || n.contains("filé") || n.contains("file") || n.contains("hambúrguer") ||
            n.contains("hamburguer") || n.contains("camarão") || n.contains("camarao") || n.contains("salmão") ||
            n.contains("salmao") || n.contains("sardinha") || n.contains("atum") || n.contains("bacalhau") -> AisleCategory.ACOUGUE

            n.contains("leite") || n.contains("queijo") || n.contains("manteiga") || n.contains("requeijão") ||
            n.contains("requeijao") || n.contains("iogurte") || n.contains("danone") || n.contains("creme de leite") ||
            n.contains("margarina") || n.contains("ricota") || n.contains("mussarela") || n.contains("mozzarella") ||
            n.contains("provolone") || n.contains("parmesão") || n.contains("parmesao") || n.contains("gorgonzola") ||
            n.contains("prato") || n.contains("minas") || n.contains("coalho") || n.contains("yakult") ||
            n.contains("leite fermentado") || n.contains("nata") || n.contains("requeijao") || n.contains("petit suisse") ||
            n.contains("chanti") || n.contains("chantilly") || n.contains("coalhada") || n.contains("soro") -> AisleCategory.LATICINIOS

            n.contains("pão") || n.contains("pao") || n.contains("bolo") || n.contains("biscoito") ||
            n.contains("bolacha") || n.contains("croissant") || n.contains("torrada") || n.contains("doce") ||
            n.contains("salgado") || n.contains("pão de queijo") || n.contains("pao de queijo") ||
            n.contains("baguete") || n.contains("brioche") || n.contains("broa") || n.contains("bisnaguinha") ||
            n.contains("donuts") || n.contains("tortinha") || n.contains("muffins") || n.contains("cookies") ||
            n.contains("waffle") || n.contains("panetone") || n.contains("chocotone") || n.contains("rosquinha") ||
            n.contains("cream cracker") || n.contains("recheado") -> AisleCategory.PADARIA

            n.contains("cerveja") || n.contains("refrigerante") || n.contains("suco") || n.contains("água") ||
            n.contains("agua") || n.contains("vinho") || n.contains("vodka") || n.contains("refri") ||
            n.contains("energético") || n.contains("energetico") || n.contains("chá") || n.contains("cha") ||
            n.contains("gatorade") || n.contains("isotônico") || n.contains("isotonico") || n.contains("tônica") ||
            n.contains("tonica") || n.contains("champagne") || n.contains("espumante") || n.contains("cachaça") ||
            n.contains("cachaca") || n.contains("whisky") || n.contains("licor") || n.contains("rum") ||
            n.contains("gin") || n.contains("néctar") || n.contains("nectar") || n.contains("polpa") -> AisleCategory.BEBIDAS

            n.contains("arroz") || n.contains("feijão") || n.contains("feijao") || n.contains("macarrão") ||
            n.contains("macarrao") || n.contains("óleo") || n.contains("oleo") || n.contains("azeite") ||
            n.contains("sal") || n.contains("açúcar") || n.contains("acucar") || n.contains("café") ||
            n.contains("cafe") || n.contains("farinha") || n.contains("molho") || n.contains("ketchup") ||
            n.contains("mostarda") || n.contains("maionese") || n.contains("milho") || n.contains("ervilha") ||
            n.contains("aveia") || n.contains("chocolate") || n.contains("miojo") || n.contains("tempero") ||
            n.contains("trigo") || n.contains("massa") || n.contains("fermento") || n.contains("extrato") ||
            n.contains("vinagre") || n.contains("pipoca") || n.contains("canola") || n.contains("girassol") ||
            n.contains("milho de pipoca") || n.contains("achocolatado") || n.contains("toddy") ||
            n.contains("nescau") || n.contains("leite em pó") || n.contains("leite em po") || n.contains("leite condensado") ||
            n.contains("doce de leite") || n.contains("geléia") || n.contains("geleia") || n.contains("mel") ||
            n.contains("cereal") || n.contains("granola") || n.contains("creme de avelã") || n.contains("creme de avela") ||
            n.contains("nutella") || n.contains("amendoim") || n.contains("castanha") || n.contains("batata palha") ||
            n.contains("salgadinho") -> AisleCategory.MERCEARIA

            n.contains("sabão") || n.contains("sabao") || n.contains("detergente") || n.contains("amaciante") ||
            n.contains("desinfetante") || n.contains("cloro") || n.contains("água sanitária") || n.contains("agua sanitaria") ||
            n.contains("esponja") || n.contains("vassoura") || n.contains("rodo") || n.contains("pano de prato") ||
            n.contains("saco de lixo") || n.contains("multiúso") || n.contains("multiuso") || n.contains("lustra") ||
            n.contains("álcool") || n.contains("alcool") || n.contains("limpador") || n.contains("inseticida") ||
            n.contains("removedor") || n.contains("saponáceo") || n.contains("saponaceo") || n.contains("naftalina") ||
            n.contains("saco plástico") || n.contains("saco plastico") || n.contains("prendedor") ||
            n.contains("varal") || n.contains("desodorizador") || n.contains("bom ar") -> AisleCategory.LIMPEZA

            n.contains("shampoo") || n.contains("xampu") || n.contains("condicionador") || n.contains("sabonete") ||
            n.contains("pasta de dente") || n.contains("creme dental") || n.contains("escova") || n.contains("fio dental") ||
            n.contains("desodorante") || n.contains("perfume") || n.contains("papel higiênico") || n.contains("papel higienico") ||
            n.contains("absorvente") || n.contains("gilete") || n.contains("lâmina") || n.contains("lamina") ||
            n.contains("fralda") || n.contains("lenço umedecido") || n.contains("lenco umedecido") ||
            n.contains("algodão") || n.contains("algodao") || n.contains("cotonete") || n.contains("hastes flexíveis") ||
            n.contains("hastes flexiveis") || n.contains("antisséptico") || n.contains("antisseptico") ||
            n.contains("protetor solar") || n.contains("bronzeador") || n.contains("hidratante") ||
            n.contains("creme de barbear") || n.contains("enxaguante") || n.contains("colgate") ||
            n.contains("espuma") || n.contains("talco") || n.contains("esmalte") || n.contains("maquiagem") ||
            n.contains("preservativo") || n.contains("camisinha") -> AisleCategory.HIGIENE

            n.contains("sorvete") || n.contains("pizza") || n.contains("lasanha") || n.contains("nuggets") ||
            n.contains("batata congelada") || n.contains("pote de sorvete") || n.contains("congelado") ||
            n.contains("pão congelado") || n.contains("pao congelado") || n.contains("salgadinho congelado") ||
            n.contains("torta congelada") || n.contains("refeição pronta") || n.contains("refeicao pronta") ||
            n.contains("açai") || n.contains("açaí") || n.contains("picolé") || n.contains("picole") -> AisleCategory.CONGELADOS

            else -> AisleCategory.OUTROS
        }
    }
}

@Entity(tableName = "shopping_trips")
data class ShoppingTrip(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val title: String,
    val date: Long = System.currentTimeMillis(),
    val budgetLimit: Double,
    val isCompleted: Boolean = false,
    val notes: String = ""
)

@Entity(tableName = "shopping_items")
data class ShoppingItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val tripId: Long,
    val name: String,
    val category: String, // Value name from AisleCategory enum
    val priority: String = "MEDIUM", // "LOW", "MEDIUM", "HIGH"
    val targetQuantity: Int = 1,
    val isChecked: Boolean = false, // Em carrinho
    val estimatedPrice: Double = 0.0,
    val actualPrice: Double = 0.0,
    val actualQuantity: Int = 1
)
