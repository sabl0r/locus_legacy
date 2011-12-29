
var de = de || {};
de.weizenbaron = de.weizenbaron || {};
de.weizenbaron.Locus = de.weizenbaron.Locus || {};

$.Class.extend('de.weizenbaron.Locus.Map', {}, {

	/**
	 * Konstruktor
	 *
	 * @param {Array} element DOM-Element, in dem die Karte angezeigt werden soll
	 * @param {Object} options Verschiedene Optionen, momentan unterstützt:
	 *                         center: Koordination des Startpunkts als Objekt mit der Notation
	 *                                 "12345,12345" oder {lat: 12345, lng: 12345}
	 *                         destinationElement: ID des Formular-Elements, das den Index des Ziel-Markers enthält
	 *                                             z.B. eine Select-Box
	 *                         styles: Benutzerdefinierte Karten Stile
	 */
	init: function(element, options){
	
		/**
		 * Verschiedene Optionen
		 **/
		this._options = {

			/**
			 * Koordination des Startpunkts als Objekt mit der Notation
			 * {lat: 12345, lng: 12345}
			 */
			center: null,
			
			/**
			 * ID des Formular-Elements, das den Index des Ziel-Markers enthält
			 * z.B. eine Select-Box
			 */
			destinationElement: null,
			
			/**
			 * Benutzerdefinierte Karten Stile
			 */
			styles: []

		};
		$.extend(true, this._options, options || {});

		/**
		 * DOM-Element, in dem die Karte angezeigt werden soll
		 */
		this._element = element;

		/**
		 * MarkerManager Objekt
		 */
		this._markerManager = null;

		/**
		 * Marker, die auf der Karte angezeigt werden
		 */
		this._markers = [];

		/**
		 * google.maps.DirectionsRenderer Objekt
		 */
		this._directionsDisplay = null;

		if(typeof(this._options.center) === 'string'){
			this._options.center = {
				lat: this._options.center.split(',')[0],
				lng: this._options.center.split(',')[1]
			}
		}
		
		/*
		 * Karten Optionen
		 */
		var mapOptions = {
			center: new google.maps.LatLng(this._options.center.lat, this._options.center.lng),
			zoom: 16,
			mapTypeId: google.maps.MapTypeId.ROADMAP,
			mapTypeControlOptions: {
				mapTypeIds: [google.maps.MapTypeId.ROADMAP, google.maps.MapTypeId.SATELLITE, google.maps.MapTypeId.HYBRID]
			}
		};
		
		/*
		 * google.maps.Map Objekt
		 */
		this._map = new google.maps.Map(this._element[0], mapOptions);
		
		/*
		 * Benutzerdefinierte Karten Stile anwenden
		 */
		if(this._options.styles.length > 0){
			this._map.mapTypes.set('customMapId', new google.maps.StyledMapType(this._options.styles, { name: 'Karte', alt: 'Stadtplan anzeigen' }));
			mapOptions.mapTypeControlOptions.mapTypeIds = ['customMapId', google.maps.MapTypeId.SATELLITE, google.maps.MapTypeId.HYBRID];
			this._map.setOptions(mapOptions);
			this._map.setMapTypeId('customMapId');
		}		
		
	},

	/**
	 * Erzeugt einen MarkerManager
	 *
	 * @param {Object} options verschiedene Optionen
	 *                         http://google-maps-utility-library-v3.googlecode.com/svn/tags/markermanager/1.0/docs/reference.html
	 */
	markerManager: function(options){
		
		this._markerManager = new MarkerManager(this._map, options);
		
	},

	/**
	 * Fügt einen Marker hinzu und gibt ihn zurück
	 *
	 * @param {String} latlng Koordination des Markers als String oder Object
	 * @param {String} info Inhalt des Info-Windows
	 * @param {Object} options Verschiedenen Optionen
	 *                         http://code.google.com/intl/de-DE/apis/maps/documentation/javascript/reference.html#MarkerOptions
	 * @param {Object} events Event-Handler für den Marker
	 *                        http://code.google.com/intl/de-DE/apis/maps/documentation/javascript/reference.html#Marker
	 * @return {google.maps.Marker}
	 */
	addMarker: function(latlng, info, options, events){

		if(typeof(latlng) === 'string'){
			latlng = {
				lat: latlng.split(',')[0],
				lng: latlng.split(',')[1]
			}
		}

		var marker = new google.maps.Marker($.extend({
			position: new google.maps.LatLng(latlng.lat, latlng.lng)
		}, options || {}));

		marker.infoWindow = new google.maps.InfoWindow({
			content: info
		});

		google.maps.event.addListener(marker, 'click', $.proxy(function(){
			marker.infoWindow.open(this._map, marker);
		}, this));

		$.each(events || {}, function(event){
			if(typeof this != 'undefined'){
				google.maps.event.addListener(marker, event, this);
			}
		});

		this._markers.push(marker);
		return marker;

	},
	
	/**
	 * Gibt alle Marker zurück
	 * 
	 * @return {Array}
	 */
	getMarkers: function(){

		return this._markers;
	
	},

	/**
	 * Fügt alle Marker des MarkerManagers der Karte hinzu
	 */
	initMarkers: function(){

		google.maps.event.addListener(this._markerManager, 'loaded', $.proxy(function(){
			this._markerManager.addMarkers(this._markers, 1, 17);
			this._markerManager.refresh();
			if(this._markers.length > 1){
				this.fitMap();
			}
		}, this));

	},
	
	/**
	 * Öffnet die Info-Fenster von bestimmten Markern.
	 * 
	 * Event-Handler nötig, damit sich die Karte richtig an die Info-Fenster
	 * anpasst. Siehe:
	 * http://groups.google.com/group/google-maps-api/msg/1b9d46619d68d02e
	 * 
	 * @param {Array} markers Marker, deren Info-Fenster geöffnet werden sollen
	 */	
	showInfoWindows: function(markers){
		
		var l = google.maps.event.addListener(this._map, 'idle', function(){
			$.each(markers, function(){
				google.maps.event.trigger(this, "click");
			});
			google.maps.event.removeListener(l);
		});
		
	},

	/**
	 * Passt den Zoom und die Position der Karte an, damit alle enthaltenen
	 * Punkte zu sehen sind
	 *
	 * @param {Object} markers Die Punkte, die berücksichtigt werden sollen
	 *                         (optional). Wird der Parameter weggelassen,
	 *                         werden die Marker der aktuellen Karte verwendet.
	 */
	fitMap: function(markers){

		markers = markers || this._markers;
		var bounds = new google.maps.LatLngBounds();
		$.each(markers, function(){
			bounds.extend(this.getPosition());
		})
		this._map.fitBounds(bounds);

	},

	/**
	 * Berechnet eine Route zwischen zwei Punkten und zeigt diese an
	 *
	 * @param {String} location location des Startpunkts
	 * @param {String} street Straße und Hausnummer des Startpunkts (optional)
	 * @param {String} postalCode postalCode des Startpunkts (optional)
	 */
	calculateRoute: function(location, street, postalCode){

		var destination = this._markers[$('#' + this._options.destinationElement).val()];
		var from = '';

		if(street && street.strip() != ''){
			from += street.strip() + ',';
		}

		if(postalCode && postalCode.strip() != ''){
			from += postalCode.strip() + ',';
		}

		from += location.strip() + ',Deutschland';

		// Alte Route löschen, falls vorhanden
		if(this._directionsDisplay){
			this._directionsDisplay.setMap(null);
			$('#route').empty();
		}

		// Neue Route erzeugen
		this._directionsDisplay = new google.maps.DirectionsRenderer({
			map: this._map,
			panel: $('#route')[0]
		});

		// Route berechnen
		(new google.maps.DirectionsService()).route({
			origin: from,
			destination: destination.getPosition().toString(),
			travelMode: google.maps.DirectionsTravelMode.DRIVING,
			region: 'de',
			language: 'de'
		}, $.proxy(function(response, status){
			if(status == google.maps.DirectionsStatus.OK){
				this._directionsDisplay.setDirections(response);

				// Info-Fenster ausblenden
				$.each(this._markers, function(){
					this.infoWindow.close();
				});
				
				// Info-Fenster des Ziels einblenden
				google.maps.event.trigger(destination, 'click');

				if($('#route')){
					$('#route')[0].innerHTML += '<div style="text-align: right; padding-top: 10px;"><a href="http://maps.google.de/maps?f=d&hl=de&geocode=&saddr='+from+'&daddr='+destination+'&z=12&om=1&pw=2" target="_blank" class="print">Druckansicht</a></div>';
				}
			} else {
				alert('Ihre Startadresse wurde leider nicht gefunden.');
			}
		}, this));

	},

	/**
	 * Fährt die Karte auf einen Punkt und zeigt das Info-Fenster an
	 *
	 * @param {Object} position Formular-Element, das den Index des Markers der
	 *                          gewünschten Position enthält (z.B. selectbox)
	 */
	setPosition: function(position){

		var index = $(position).val();

		// Info-Fenster ausblenden
		$.each(this._markers, function(){
			this.infoWindow.close();
		});

		if(index == -1){
			this.fitMap();
			return;
		}

		var bounds = new google.maps.LatLngBounds();
		bounds.extend(this._markers[index].getPosition());

		this._map.setZoom(16);
		this._map.setCenter(bounds.getCenter());

		google.maps.event.trigger(this._markers[index], "click");

	},

	/**
	 * Ermittelt den Geocode für eine Adresse und setzt den ersten Marker auf
	 * auf diese Position.
	 *
	 * @param {$String} address Adresse deren Geocode ermittelt werden soll
	 * @param {$Function} callback Callback-Funktion, die aufgerufen werden soll, wenn ein Ergebnis gefunden wurde
	 * @param {$Function} callback_error Callback-Funktion, die aufgerufen werden soll, wenn ein Fehler auftrat
	 */
	getGeoCode: function(address, callback, callback_error){
		
		var geocoder = new google.maps.Geocoder();
		geocoder.geocode({
			address: address
		}, $.proxy(function(results, status){
			if(status == google.maps.GeocoderStatus.OK){
				this._markers[0].setPosition(results[0].geometry.location);
				google.maps.event.trigger(this._markers[0], 'click');
				this._map.panTo(results[0].geometry.location);
				callback({latLng: results[0].geometry.location});
			} else {
				callback_error ? callback_error() : alert('Adresse nicht gefunden');
			}
		}, this));
		
	},
	
	/**
	 * Initialisiert die Karte neu
	 */
	checkResize: function(){
				
		google.maps.event.trigger(this._map, 'resize');
		this._map.setZoom(this._map.getZoom());
		
	}

});

/*
 * Als jQuery Plugin registrieren
 */
$.fn.gmap = function(options){
	return $.fn.encapsulatedPlugin('gmap', de.weizenbaron.Locus.Map, this, options);
};
