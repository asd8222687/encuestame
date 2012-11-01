/**
 * Dnd Support.
 * @author Picado, Juan juanATencuestame.org
 * @since 21/08/2011
 */
define([
     "dojo/_base/declare",
     "dojo/dnd/Source",
     "dojo/dnd/Manager",
     "dojo/_base/lang",
     "me/web/widget/pictures/Avatar",
     "me/core/enme"],
    function(
    declare,
    Source,
    Manager,
    lang,
    Avatar,
    _ENME) {

   lang.extend(Manager.manager, {

// 	   TODO: To customize the avatar
//     makeAvatar: function() {
//         return new Avatar(this);
//     },

     updateAvatar: function() {
          this.avatar.update();
     },
     // avatar's offset from the mouse
     OFFSET_X: 0,
     OFFSET_Y: 0,
     canDrop: function(flag){
         //console.debug("canDrop flag", flag);
         // summary:
         //		called to notify if the current target can accept items
         var canDropFlag = Boolean(this.target && flag);
         //console.debug("canDrop canDropFlag", canDropFlag);
         if(this.canDropFlag != canDropFlag){
             this.canDropFlag = canDropFlag;
             this.avatar.update();
         }
     },
     overSource: function(source){
         //console.debug("overSource source", source.node);
         // summary:
         //		called when a source detected a mouse-over condition
         // source: Object
         //		the reporter
         if (this.avatar) {
             this.target = (source && source.targetState != "Disabled") ? source : null;
             this.canDropFlag = Boolean(this.target);
             this.avatar.update();
         }
         dojo.publish("/dnd/source/over", [source]);
     }
});

  return declare(null, {

    /*
    *
    */
   node : null,

   /*
    *
    */
   sourceDndWidget : null,

   /*
    *
    */
   accept : [],

   /*
    *
    */
   copyOnly : false,

   /*
    *
    */
   selfCopy : false,

   /*
    *
    */
   selfAccept : true,

   /*
    *
    */
   withHandles : true,

   /*
    *
    */
   autoSync : true,

   /*
    *
    */
   isSource : true,

   /*
    *
    */
   constructor: function() {
       this.node = null;
   },

   /*
    *
    */
   enableDndSupport : function(node, customCreator) {
       this.node = node;
       var params = {
                   accept: this.accept,
                   copyOnly: this.copyOnly,
                   selfCopy : this.selfCopy,
                   selfAccept: this.selfAccept,
                   withHandles : this.withHandles,
                   autoSync : this.autoSync,
                   isSource : this.isSource
                   //creator: this.dndNodeCreator
                   };
       if (customCreator) {
           //dojo.mixin(params, { creator: this.dndNodeCreator});
       }
       console.debug("dnd params", params);
       var source  = new Source(this.node, params);
           this.sourceDndWidget = source;
           console.debug("enabled DND Source on ", this.node);
       dojo.connect(source, "onDndDrop", dojo.hitch(this, this.onDndColumn));
   },

   /*
    * on drop on folder.
    */
   onDndColumn : function(source, nodes, copy, target) {
           dojo.forEach(dojo.query(".dojoDndItemSelected"), function(item){
               dojo.removeClass(item, "dojoDndItemSelected");
           });
           dojo.forEach(dojo.query(".dojoDndItemAnchor"), function(item){
               dojo.removeClass(item, "dojoDndItemAnchor");
           });
           if(dojo.dnd.manager().target !== this.sourceDndWidget){
               return;
           }
           if(dojo.dnd.manager().target == dojo.dnd.manager().source){
               this._dndAction();
           }
   },

   /*
    *
    */
   addItems : function(array){
        this.sourceDndWidget.insertNodes(false, itemArray);
   },

   /*
    *
    */
   addItem : function(node) {
       var itemArray = [];
       itemArray.push(node);
          this.sourceDndWidget.insertNodes(false, itemArray);
  },

   /*
    *
    */
   _dndAction : function(){
        dojo.forEach(this.sourceDndWidget.getSelectedNodes(), dojo.hitch(this, function(item) {
            console.debug("DND item", item);
      }));
   },

   /*
    * dnd node creator.
    */
   dndNodeCreator : function (item, hint) {
       //console.debug("hint", hint);
       //console.debug("item", item);
       var tr = document.createElement("div");
       tr.innerHTML = "Item Dropped...";
       return {node: tr, data: item, type: "poll"};
   }

   /**
    * TODO: in this place should be move all DnD code repeated in another widgets.
    * TweetpollList.
    * DashboardLayout.
    * FolderActions.
    */

  });
});