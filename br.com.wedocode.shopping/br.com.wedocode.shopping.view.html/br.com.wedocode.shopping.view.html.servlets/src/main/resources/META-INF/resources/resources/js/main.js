!function (window, undefined) {
	var location = window.location;
	var document = window.document;

	$(window).on('hashchange', function (event) {
		var hash = location.hash;
		if (hash && hash.charAt(0) === "#") {
			$.post("do", { place: hash.substring(1) }).done($onDoResponse);
		}
	});

	window.app = {
		submitOnEnterPressed: function (e, eventId, data) {
			$submitOnKeyPress(e, 13, eventId, data);
		},

		submit: function (eventId, data) {
			data = data || {};
			data.event = String(eventId);

			$('input').each(function (__, item) {
				data[item.name] = item.value;
			})

			data.place = "";

			let hash = location.hash;
			if (hash && hash.charAt(0) === "#") {
				data.place = hash.substring(1);
			}

			$.post("do", data).done($onDoResponse);

			return false;
		}
	};

	$(document).ready(function () {
		window.app.submit(0, {});
	});

	return;

	function $onDoResponse(data) {
		$('#container').html(data);

		var hash = $('#place').val();
		if (hash !== location.hash) {
			location.hash = hash;
		}
	}

	function $submitOnKeyPress(event, key, eventId, data) {
		var keycode;
		if (event) {
			keycode = event.which;
		} else if (window.event) {
			keycode = window.event.keyCode;
		} else {
			return true;
		}

		if (keycode === key) {
			window.app.submit(eventId, data);
			return false;
		} else {
			return true;
		}
	};
}(window);