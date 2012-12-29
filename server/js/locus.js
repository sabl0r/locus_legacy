
var de = de || {};
de.weizenbaron = de.weizenbaron || {};
de.weizenbaron.Locus = de.weizenbaron.Locus || {};

$.extend(de.weizenbaron.Locus, {

	init: function(container_id, users){
		
		google.setOnLoadCallback(function(){
			google.load("maps", "3", {
				other_params: "sensor=false&language=de",
				language: "de",
				callback: function(){
					de.weizenbaron.Locus._loadJs(['js/markermanager.js','js/markerwithlabel.js'], $.proxy(function(){
						
						var map = $('#' + container_id).gmap({
							center: { lat: 0, lng: 0 }
						});
						map.markerManager({ borderPadding: 100 });

						var open = [];
						$.each(users, function(){
							var datetime = this.date.split(' ');
							var date = datetime[0].split('-');
							var time = datetime[1].substr(0, 5);
							var m = map.addMarker(
								this.latitude + ',' + this.longitude,
								'<div class="gwindow">' + this.username + '<br />' + date[2] + '.' + (date[1] - 1) + '.' + date[0] + ' ' + time + ' Uhr</div>',
								this.username,
								{}, {});
							
							map.addCircle(
								m,
								this.accuracy
								);
						});

						map.initMarkers(16);
						map.showInfoWindows(open);

					}, de.weizenbaron.Locus));
				}
			});
		});

	},

	/**
	 * Loads one or more javascript file(s) dynamically asynchronously
	 * 
	 * @param {String/Array} src The file(s) or URL(s), which should be loaded
	 * @param {Object} callback The callback function which is called, when the file is completely loaded
	 * @return {Deferred}
	 */
	_loadJs: function(src, callback){
		
		if(typeof(src) === 'string'){
			src = [src];
		}
		
		var calls = [];
		$.each(src, function(){
			calls.push($.getScript(this));
		});
		
		var deferred = $.when.apply(this, calls);
		if(callback){
			deferred.then(callback);
		}
		
		return deferred;
		
	}

});

