/*
 ************************************************************************************
 * Copyright (C) 2001-2011 encuestame: open source social survey Copyright (C) 2009
 * encuestame Development Team.
 * Licensed under the Apache Software License version 2.0
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to  in writing,  software  distributed
 * under the License is distributed  on  an  "AS IS"  BASIS,  WITHOUT  WARRANTIES  OR
 * CONDITIONS OF ANY KIND, either  express  or  implied.  See  the  License  for  the
 * specific language governing permissions and limitations under the License.
 ************************************************************************************
 */
dojo.provide("encuestame.org.core.commons.poll.PollNavigate");

dojo.require("encuestame.org.core.shared.utils.TableLinkedList");
dojo.require("encuestame.org.main.EnmeMainLayoutWidget");
dojo.require("encuestame.org.core.shared.utils.FoldersActions");;
dojo.require("encuestame.org.core.shared.utils.FilterList");
dojo.require("encuestame.org.core.shared.utils.TableLinkedList");
dojo.require("encuestame.org.core.shared.utils.DropDownMenuSelect");
dojo.require("encuestame.org.core.shared.utils.More");
dojo.require("encuestame.org.core.commons.support.PanelWipe");
dojo.require("encuestame.org.core.shared.utils.StandBy");
dojo.require("encuestame.org.core.commons.chart.ChartLayerSupport");

dojo.require("dijit.InlineEditBox");
dojo.require("dijit.form.Textarea");
dojo.require("dojox.widget.Toaster");
dojo.require("dijit.form.CheckBox");

/**
 * Poll Administration Widget.
 */
dojo.declare(
    "encuestame.org.core.commons.poll.PollNavigate",
    [encuestame.org.main.EnmeMainLayoutWidget,
     encuestame.org.core.shared.utils.FilterList],{

        /**
         * Poll navigate template.
         */
        templatePath: dojo.moduleUrl("encuestame.org.core.commons.poll", "templates/pollNavigate.html"),

        /*
         *
         */
        _rows : [],

        /**
         * Override property field.
         */
        property : "poll",

        /**
         * Override  folder scope field.
         */
        folder_scope : "poll",

        /*
         *
         */
        _cache_items : [],

        /**
         * Poll Navigate default parameters.
         */
        _params : { typeSearch : "BYOWNER", keyword : null, max : 10, start : 0},

        /**
         * Post Create Cycle Life.
         */
        postCreate : function() {
            var def = new dojo.Deferred();
            //required subscribe to filter support.
            //should be in the parent class??
            dojo.subscribe("/encuestame/filter/list/call", this, "_callFilterList");
            try {
                def.then(dojo.hitch(this, this._callServiceSearch));
                def.then(this._printRows);
                def.callback(true);
            } catch(e) {
               def.errback(new Error("load poll failed."));
            }
            //enable folder support.
            if (this.folder_support && this._folder) {
               this.enableFolderSupport();
            }
            //enable more support.
            if (this.enable_more_support) {
                this.enableMoreSupport(this._params.start, this._params.max, this._more);
            }

            //dojo.addOnLoad(function() {
            //    dojo.connect(dojo.byId('strapline'), 'onclick', function(event) {
            //        dojo.publish('myMessages', [{ message: 'Qwerty', type: "error", duration: 0}]);
            //    });
            //});
        },

        /**
         * Function to clean _items node.
         */
        _empty : function() {
            console.debug("empty items");
            dojo.empty(this._items);
        },

        /**
         * Subscribe function on filter search
         * @param typeSearch set the type of search
         */
        _callFilterList : function(typeSearch) {
            this._params.typeSearch = typeSearch;
            //console.info("_callFilterList", typeSearch);
            ///console.info("_callFilterList", this._params);
            this._callServiceSearch();
        },

        /*
         *
         */
        _afterEach : function() {
            //var more = new encuestame.org.core.shared.utils.More();
        },

        /**
         * Call a service to retrieve a list of poll based on a previous filter parameters.
         */
        _callServiceSearch : function() {
            dojo.hitch(this, this.loadItems(encuestame.service.list.listPoll));
        },


        /**
         * customize service params.
         */
        getParams : function() {
            return this._params;
        },

        /**
         * The url json service.
         * @returns
         */
        getUrl : function() {
            return encuestame.service.list.listPoll;
        },


        /**
         * Create a new PollNavigateItem.
         */
        processItem : function(/** poll data**/  data, /** position **/ index) {
            //console.info(data);
            var row = new encuestame.org.core.commons.poll.PollNavigateItem({ data: data});
            this._rows.push(row);
            dojo.place(row.domNode, this._items);
            //console.info("row added");
        },

        /**
         *
         */
        _printRows : function() {
             dojo.forEach(this._rows,
                  dojo.hitch(this, function(data, index) {
                      this._cache_items.push(data);
             }));
        }
});

/**
 * Poll Navigate Item.
 */
dojo.declare(
        "encuestame.org.core.commons.poll.PollNavigateItem",
            [encuestame.org.main.EnmeMainLayoutWidget], {

        /*
         *
         */
        templatePath: dojo.moduleUrl("encuestame.org.core.commons.poll", "templates/pollListItem.html"),

        /*
         *
         */
        data : null,

        /*
         *
         */
        _standBy : null,

        /**
         * Poll detail widget reference.
         */
        widget_detail : null,

        /**
         * Post create cycle life.
         */
        postCreate : function() {
            //console.debug("row data", this.data);
            var panel = new encuestame.org.core.commons.support.PanelWipe(this._more, null, null);
            //add event on click edit link
            panel.connect(this._edit, dojo.hitch(this, this._callEditInfo));
            //this._standBy = dijit.byId("standby_"+this.id);
            this.widget_detail = new encuestame.org.core.commons.poll.PollNavigateItemDetail({ data : this.data});
            dojo.addClass(this.widget_detail.domNode, "hidden");
            dojo.place(this.widget_detail.domNode, this._more);
        },

        /**
         * Call Edito Info.
         */
        _callEditInfo : function() {
            //console.info("_callEditInfo");
            var load = dojo.hitch(this, function(data) {
                dojo.removeClass(this.widget_detail.domNode, "hidden");
                data = {
                        answers : [
                                   {
                                       answer: "answer 1",
                                       type : "text",
                                       percent : 43,

                                   },
                                   {
                                       answer: "answer 2",
                                       type : "text",
                                       percent : 13,

                                   },
                                   {
                                       answer: "answer 3",
                                       type : "text",
                                       percent : 3,

                                   },
                                   {
                                       answer: "answer 4",
                                       type : "text",
                                       percent : 14,

                                   },
                                   {
                                       answer: "answer5",
                                       type : "text",
                                       percent : 27,

                                   }
                                   ]
                };
                this.widget_detail.setResults(data);
                //this._standBy.stop();
                //console.info("poll detail", data);
            });
            var error = dojo.hitch(this, function(error) {
                //this._standBy.stop();
                console.debug("error", error);
            });
            var params = {
                    id : this.data.id
            };
            //this._standBy.startup();
            //this._standBy.start();
            dojo.addClass(this.widget_detail.domNode, "hidden");
            encuestame.service.xhrGet(encuestame.service.list.poll.detail, params, load, error);
        },
});

/**
 * Poll Navigation Item Detail Widget.
 */
dojo.declare(
        "encuestame.org.core.commons.poll.PollNavigateItemDetail",
            [encuestame.org.main.EnmeMainLayoutWidget,
             encuestame.org.core.commons.chart.ChartLayerSupport], {

       /**
        * HTML Template.
        */
       templatePath: dojo.moduleUrl("encuestame.org.core.commons.poll", "templates/pollListItemDetail.html"),


       postCreate : function() {

       },

       /**
        *
        */
       setResults : function(data) {
           var nodeId = this.id+"_chart";
           dojo.empty(dojo.byId(nodeId));

           var answer = ["Si", 40, "#A6B4BF"];
           var answer1 = ["No", 10, "#FEFEF"];
           var answer2 = ["TV", 50, "#44444"];

           this.widgetChart = this.buildChart({id : nodeId, results : [answer,answer1,answer2]});
           this.renderChart(this.widgetChart);
       }
});