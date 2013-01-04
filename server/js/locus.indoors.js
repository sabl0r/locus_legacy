
var de = de || {};
de.weizenbaron = de.weizenbaron || {};
de.weizenbaron.LocusIndoors = de.weizenbaron.LocusIndoors || {};

$.extend(de.weizenbaron.LocusIndoors, {

	init: function(refresh){

		window.setTimeout(function(){
			location.reload();
		}, refresh)

	}

});

