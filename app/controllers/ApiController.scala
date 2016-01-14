package controllers

import controllers.mysql._
import play.api.libs.json.Json
import play.api.mvc._

class ApiController extends Controller {
	def extcards = UserAction.async { implicit req =>
		val ext = req.queryString.get("id").get.head
		val user = if (req.authenticated) req.user.name else ""
		sql"""
			SELECT c.identifier, c.name, c.color, c.rarity, IFNULL(col.quantity, 0)
			FROM complete_cards AS c
			LEFT JOIN collections AS col ON col.card = c.id AND col.version = c.version AND col.user = $user
			WHERE extension_id = $ext
			ORDER BY identifier ASC
		""".as[(String, String, String, String, Int)].run.map { rows =>
			Json.toJson(rows.map { case (id, name, color, rarity, quantity) =>
				Json.obj(
					"id" -> id,
					"name" -> name,
					"color" -> color,
					"rarity" -> rarity,
					"quantity" -> quantity
				)
			})
		}.map { array =>
			Ok(array)
		}
	}
}
