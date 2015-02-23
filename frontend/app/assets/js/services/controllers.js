/**
 * Services controllers.
 */
define([ 'underscore' ], function() {
  'use strict';

  var ServicesCtrl = function($scope, playRoutes) {

  };
  ServicesCtrl.$inject = [ '$scope', 'playRoutes' ];

  var FactorialCtrl = function($scope, playRoutes) {
    /** Creating the websocket to watch for cluster node changes */
    var websocketUrl = playRoutes.controllers.services.Factorial.websocket().webSocketUrl();
    var ws = new WebSocket(websocketUrl);

    /**
     * This could be refactored into a nice angular service, but for the sake of
     * simplicity, we put it all in here
     */
    ws.onmessage = function(msg) {
      var data = JSON.parse(msg.data);
      $scope.$apply(function(){
        var result = data.result;
        if(data.result.length > 10) {
          result = data.result.substr(1,7) + '...';
        }
        
        $scope.result = result;
        $scope.done = $scope.done + 1;
        if($scope.done == $scope.times) {
           $scope.finished = new Date();
           $scope.runtime = ($scope.finished.getTime() - $scope.start.getTime()) / 1000;
        }
      });
    };
    
    /**
     * Starting n-times calculations
     */
    $scope.startCalculation = function() {
      $scope.done = null;
      $scope.result = null;
      $scope.runtime = null;
      $scope.start = new Date();
      var times = $scope.times;
      var fac = $scope.factorial;
      for(var i = 0; i < times; i++) {
        ws.send(JSON.stringify({
          n : fac
        }));
      }
    };

  };
  FactorialCtrl.$inject = [ '$scope', 'playRoutes' ];

    var RastriginCtrl = function ($scope, playRoutes) {
        var workProducerWebsocketUrl = playRoutes.controllers.services.Rastrigin.workProducerWebsocket().webSocketUrl();
        var workResultConsumerWebsocketUrl = playRoutes.controllers.services.Rastrigin.workResultConsumerWebsocket().webSocketUrl();
        var workProducerWs = new WebSocket(workProducerWebsocketUrl);
        var workResultConsumerWs = new WebSocket(workResultConsumerWebsocketUrl);

        $scope.cycles = 10;
        $scope.dimension = 2;
        $scope.size = 100;

        workResultConsumerWs.onmessage = function (msg) {
            var data = JSON.parse(msg.data);
            $scope.$apply(function () {
                $scope.result = data.value;
                $scope.resultCycles = data.cycles;
                $scope.finished = new Date();
                $scope.runtime = ($scope.finished.getTime() - $scope.start.getTime()) / 1000;
            });
        };

        /**
         * Starting n-cycles reproduction
         */
        $scope.run = function () {
            $scope.done = null;
            $scope.result = null;
            $scope.runtime = null;
            $scope.start = new Date();

            workProducerWs.send(JSON.stringify({
                n: $scope.dimension, cycles: $scope.cycles, size: $scope.size
            }));
        };

    };
    RastriginCtrl.$inject = ['$scope', 'playRoutes'];



  return {
      FactorialCtrl: FactorialCtrl,
      RastriginCtrl: RastriginCtrl
  };

});
