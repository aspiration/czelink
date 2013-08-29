requirejs.config({
	baseUrl : 'resources/js/lib',
	paths : {
		views : '../../views',
		widgets : '../../widgets'
	},
	shim : {
		"angular" : {
			exports : "angular"
		},
		"jquery" : {
			exports : "$"
		},
		"bootstrap" : {
			exports : "bootstrap"
		},
		"rangy-core" : {
			exports : "rangy"
		},
		"rangy-cssclassapplier" : {
			exports : "cssclassapplier"
		}
	}
});

requirejs([ 'angular', 'jquery', 'domReady', 'require', 'uuid' ], function(
		angular, jquery, domReady, require, uuid) {

	domReady(function() {
		// require bootstrap
		require([ 'bootstrap', 'rangy-core', 'orchestration', 'navigation' ],
				function(bootstrap, rangy, orchestration, navigation) {
					// no-operation.
				});
	});
});