var calendar;

AllCatalogs = function( catalogs_object ) {
   var catalogs = new Array();
   $.each( catalogs_object, function( index, value ) {    
      catalog = new Catalog( value );
      catalogs.push( catalog );
   });
   this.catalogs = catalogs;
   
};

Catalog = function( catalogObj ) {
    this._temporal        = {};
    
    if ( catalogObj !== undefined && catalogObj[ 'http://purl.org/dc/terms/' ] !== undefined ) {
        if(typeof catalogObj[ 'http://purl.org/dc/terms/' ][ 'creator' ] !== "undefined")
            this._creator         = catalogObj[ 'http://purl.org/dc/terms/' ][ 'creator' ]   [ 0 ][ 'value' ] || '';
        else
            this._creator = '';

        if(typeof catalogObj[ 'http://purl.org/dc/terms/' ][ 'identifier' ] !== "undefined")
            this._identifier      = catalogObj[ 'http://purl.org/dc/terms/' ][ 'identifier' ][ 0 ][ 'value' ] || 0;
        else
            this._identifier = 0;

        if(typeof catalogObj[ 'http://purl.org/dc/terms/' ][ 'spatial' ] !== "undefined")
            this._spatial         = catalogObj[ 'http://purl.org/dc/terms/' ][ 'spatial' ]   [ 0 ][ 'value' ] || '';
        else
            this._spatial = '';

        if(typeof catalogObj[ 'http://purl.org/dc/terms/' ][ 'title' ] !== "undefined")
            this._title           = catalogObj[ 'http://purl.org/dc/terms/' ][ 'title' ]     [ 0 ][ 'value' ] || '';
        else
            this._title = '';

        // temporal value, (ej: "start=2013-03-14T10:00:00Z; end=2013-03-14T10:15:00Z; scheme=W3C-DTF;")
        var myString = catalogObj[ 'http://purl.org/dc/terms/' ][ 'temporal' ]  [ 0 ][ 'value' ] || 0;

    //    var myRegexp = /^start=(.*);\ end=(.*);\ scheme=(.*);/;
        var myRegexp = /start=(\d{4}-[01]\d-[0-3]\dT[0-2]\d:[0-5]\d\:\d+([+-][0-2]\d:[0-5]\d|Z{0,1}))/;
        var match = myRegexp.exec( myString );
        if( !match || typeof match[1] === "undefined" ) {
            result = 0; 
        } else { 
            result = match[ 1 ];
        }
        this._temporal._start = fromUTCDateString( match[ 1 ] ) || 0;

        var myRegexp = /end=(\d{4}-[01]\d-[0-3]\dT[0-2]\d:[0-5]\d\:\d+([+-][0-2]\d:[0-5]\d|Z{0,1}))/;
        var match = myRegexp.exec( myString );
        if( !match || typeof match[1] === "undefined" ) {
            result = 0; 
        } else { 
            result = match[ 1 ];
        }
        this._temporal._end = fromUTCDateString( match[ 1 ] ) || 0;

        var myRegexp = /scheme=(.*);/;
        var match = myRegexp.exec( myString );
        if( !match || typeof match[1] === "undefined" ) {
            result = 0; 
        } else { 
            result = match[ 1 ];
        }
        this._temporal._scheme = match[ 1 ] || 0;
    
    }
   /*
    * GETTERS & SETTERS
    */

   // function creator
   this.creator = function () {
      if ( arguments.length === 1 ) {
         var val = arguments[ 0 ];
         if ( typeof val !== "undefined" && val !== this._creator ) {
             this._creator = val;
         }
     } else {
         return this._creator;
     }
   };

   // function identifier
   this.identifier = function () {
      if ( arguments.length === 1 ) {
         var val = arguments[ 0 ];
         if ( typeof val !== "undefined" && val !== this._identifier ) {
             this._identifier = val;
         }
     } else {
         return this._identifier;
     }
   };

   // function spatial
   this.spatial = function () {
      if ( arguments.length === 1 ) {
         var val = arguments[ 0 ];
         if ( typeof val !== "undefined" && val !== this._spatial ) {
             this._spatial = val;
         }
     } else {
         return this._spatial;
     }
   };

   // function start
   this.start = function () {
      if ( arguments.length === 1 ) {
         var val = arguments[ 0 ];
         if ( typeof val !== "undefined" && val !== this._temporal._start ) {
             this._temporal._start = val;
         }
     } else {
         return this._temporal._start;
     }
   };

   // function end
   this.end = function () {
      if ( arguments.length === 1 ) {
         var val = arguments[ 0 ];
         if ( typeof val !== "undefined" && val !== this._temporal._end ) {
             this._temporal._end = val;
         }
     } else {
         return this._temporal._end;
     }
   };
   // function scheme
   this.scheme = function () {
      if ( arguments.length === 1 ) {
         var val = arguments[ 0 ];
         if ( typeof val !== "undefined" && val !== this._temporal._scheme ) {
             this._temporal._scheme = val;
         }
     } else {
         return this._temporal._scheme;
     }
   };

   // function title
   this.title = function () {
      if ( arguments.length === 1 ) {
         var val = arguments[ 0 ];
         if ( typeof val !== "undefined" && val !== this._title ) {
             this._title = val;
         }
     } else {
         return this._title;
     }
   };


   // END OF GETTERS & SETTERS

}; // END OF Catalog

var agents_color = new Array();
  
function set_agents_colors( agents ) {
   
   //var colors = ['darkorange', 'blue', 'fuchsia', 'green', 'darkcyan', 'maroon', 'navy', 'olive', 'purple', 'red', 'silver', 'teal', 'MediumVioletRed', 'tomato' ];
   var colors = [ 'Blue',   'CadetBlue', 'Chartreuse',  'Coral', 'CornflowerBlue', 'Crimson', 'DarkCyan', 'DarkGoldenRod', 'DarkGray', 'DarkGreen', 'DarkKhaki', 'DarkMagenta', 'DarkOliveGreen', 'Darkorange', 'DarkOrchid', 'DarkRed', 'DarkSalmon', 'DarkSeaGreen', 'DarkSlateGray', 'DarkTurquoise', 'DarkViolet', 'DeepPink', 'DeepSkyBlue', 'DimGray', 'DimGrey', 'DodgerBlue', 'FloralWhite', 'ForestGreen', 'Fuchsia', 'Gold', 'GoldenRod', 'Gray', 'Green', 'HotPink', 'IndianRed ', 'Indigo  ', 'LimeGreen', 'Magenta', 'Maroon', 'MediumAquaMarine', 'MediumBlue', 'MediumOrchid', 'MediumPurple', 'MediumSeaGreen', 'MediumSlateBlue', 'MediumSpringGreen', 'MediumTurquoise', 'MediumVioletRed', 'MidnightBlue', 'Navy', 'Olive', 'OliveDrab', 'Orange', 'OrangeRed', 'Orchid', 'Peru', 'Pink', 'Plum', 'Purple', 'Red', 'RosyBrown', 'RoyalBlue', 'SaddleBrown', 'Salmon', 'SandyBrown', 'SeaGreen', 'Sienna', 'Silver', 'Chocolate', 'SkyBlue', 'SlateBlue', 'SlateGray', 'SpringGreen', 'SteelBlue', 'Tan', 'Teal', 'Thistle', 'Tomato', 'Turquoise', 'Violet', 'YellowGreen', 'Brown', 'BlueViolet'];
   color = 100;
   $.each( agents, function(i, v){
       agents_color [v.name] = colors[(i%colors.length)];
   });
}

function search ( params ) {
    if ( params === undefined ){
        params = {};
    }
    params [ 'startsfrom' ]  = toISODate( firstDay, true );
    params [ 'endsto' ]    = toISODate( lastDay, true );

    $.ajax({
        dataType: "jsonp",
        data: params,
        jsonp: 'jsonp', 
        jsonpCallback: 'callback1',
        url: "/recordings/recordings.json"  
    }).done(function (data) {
        all_catalogs = new AllCatalogs( data['catalogs'] ); // return all the catalogs
        filter_by_agents();
    });
}


function print_html_agents( agents ) {
    html_agent = '';
    html_draggable_agent = '';
    
    $.each( agents, function( i, v ){
        html_agent = html_agent + '<li><span class="external-event ' + agents_color[v.name] + '">' + v.name + '</span><input type="checkbox" name="sel_agentes" value="' + v.name + '" id="check_'+ v.name +'" /></li>';
        html_draggable_agent = html_draggable_agent + '<li><div class="external-event">' + v.name + '</div></li>'; 
    });
    
    $('#list_filter_agents').append(html_agent);
    $('#list_draggable_agents').append(html_draggable_agent);
}

function filter_by_agents () {
    reset_calendar();
    var searched_catalogs = get_searched_catalogs( all_catalogs.catalogs ); // filter by enabled agents
    print_catalogs( searched_catalogs ); // print catalogs
}

function addEventsToCalendar( catalog ) {

    var event = {
        title   :   catalog.title(),
        id      :   catalog.identifier(),
        allDay  :   false,
        start   :   catalog.start(),
        end     :   catalog.end(),
        //url:      'scheduler.html?eventId=' + catalog.identifier() + '&edit=true',
        
        color   :   agents_color[catalog.spatial()]
    };
    
    // disable past events
    var today = new Date();
    if ( catalog.end().getTime() < today.getTime() ) {
        event.editable = false;
        event.url = 'scheduler.html?eventId=' + catalog.identifier() + '&edit=0';
    } else {
        event.editable = true;
        event.url = 'scheduler.html?eventId=' + catalog.identifier() + '&edit=1';
    }
   
    calendar.fullCalendar( 'renderEvent', event );
}


function print_catalogs( catalogs ) {
    $.each( catalogs, function( i, v ) {
        addEventsToCalendar( v );
    });
}

function get_searched_catalogs ( catalogs ) {
    var ids_enabled_agents = new Array();
    ids_enabled_agents = $( '#list_filter_agents li:not(.li_all) input[type=checkbox]:checked' ).map( function() {
        return $( this ).val();
    });
    
    var catalogs_to_show = $.grep( catalogs, function ( n, i ){ return ($.inArray(n.spatial(), ids_enabled_agents)>-1);});
    return catalogs_to_show;
}

function reset_calendar() {
    calendar.fullCalendar( 'removeEvents' );
}

function init() {
    $.ajax({
            dataType: "jsonp",
            jsonp: 'jsonp', 
            jsonpCallback: 'callback3',
            url: "/capture-admin/agents.json"    
    }).done( function ( data ) {
        var agents = process_mh_array_response(data.agents.agent);
        set_agents_colors( agents );
        print_html_agents( agents );
        
        var urlVars = getUrlVars();
        if( urlVars[ 'agent' ] !== undefined) {
            // select agent in the panel
            var selectedAgent = urlVars[ 'agent' ];
            $('#check_'+selectedAgent).prop( 'checked', true );
        } else {
            // select all agents
            $( '#filter input[type=checkbox]' ).prop( 'checked', true );
        }
            
        init_calendar();
    });
}



function getFirstDay() {
    var firstDay;
    var dateToday;
            
    if (calendar === undefined || calendar.fullCalendar( 'getDate' ) === undefined) {
        dateToday= new Date();
    } else {
        dateToday = calendar.fullCalendar( 'getDate' );
    }

    if ( calendarCurrentView === 'agendaDay') {
        firstDay = dateToday;
    } else if ( calendarCurrentView === 'agendaWeek') {
        var day = dateToday.getDay();
        var diff = dateToday.getDate() - day + (day === 0 ? -6:1); // adjust when day is Sunday
        dateToday.setDate( diff );
        firstDay = dateToday;
    } else {    // month view
        firstDay = new Date(dateToday.getFullYear(), dateToday.getMonth(), 1);
    }
    
    firstDay.setHours(00,00,00,000);
    return firstDay;
}

function getLastDay() {
    var lastDay;
    var dateToday;
            
    if (calendar === undefined || calendar.fullCalendar( 'getDate' ) === undefined)
        dateToday= new Date();
    else{
        dateToday = calendar.fullCalendar( 'getDate' );
    }

    if ( calendarCurrentView === 'agendaDay') {
        lastDay = dateToday;
    } else if ( calendarCurrentView === 'agendaWeek') {
        var day = dateToday.getDay();
        var diff = dateToday.getDate() - day + (day === 0 ? 0:+7); // adjust when day is Sunday
        dateToday.setDate( diff );
        lastDay = dateToday;
    } else {    // month view
        lastDay = new Date(dateToday.getFullYear(), dateToday.getMonth() + 1, 0);
    }
    lastDay.setHours(23,59,59,999);
    return lastDay;  
    
}


var calendarDefaultView = 'agendaWeek';
var calendarCurrentView = calendarDefaultView;
var firstDay            = getFirstDay();
var lastDay             = getLastDay();

function init_calendar() {
   $( ".datepicker" ).datepicker({ dateFormat: 'dd/mm/yy' });

   calendar = $( '#calendar' ).fullCalendar({
        //defaultView: 'agendaWeek',
        defaultView: calendarDefaultView,
        viewDisplay: function(view) {
            calendarCurrentView = view.name;
            firstDay = getFirstDay();
            lastDay = getLastDay();
            if( calendar!== undefined ) { 
                search(); /* prevent second run when calendar loads (bug?) */ 
            }
        },
        header: {
            left: 'prev,next today',
            center: 'title',
            right: 'agendaDay, agendaWeek, month'
        },
        disableResizing: true,
        disableDragging: true,
        firstDay: 1, // TODO: configuration field
        allDaySlot: false,
        droppable: false
  
   });

   calendar.fullCalendar('gotoDate', firstDay);
   view = calendar.fullCalendar( 'getView' );

   $( '#from_date' ).val( ( "0" + view.start.getDate()).slice( -2 ) + "/" + ( "0" + ( view.start.getMonth()+1 )).slice( -2 ) + "/" + view.start.getFullYear());
   $( '#to_date ').val( ( "0" + view.end.getDate()).slice( -2 ) + "/" + ( "0" + ( view.end.getMonth()+1 )).slice( -2 ) + "/" + view.end.getFullYear());
       
}



$( document ).ready( function() {
   $( '#search_input' ).keypress( function( e ){
       if ( e.which === 13 ) {
        reset_calendar();
        params = {};
         if( $( '#search_input' ).val()!== '' ) {
             params.q = $( '#search_input' ).val();
         }
         search( params );    
       }
   });
   $( '#search_input' ).focusout( function( e ){
        reset_calendar();
        params = {};
         if( $( '#search_input' ).val()!== '' ) {
             params.q = $( '#search_input' ).val();
         }
         search( params );    
   });

    $( '#filter' ).on( "click", "input[type=checkbox]", function( event ){
       var clicked_value = $( this ).val();
       var clicked = $( this ).is( ':checked' );
       if ( clicked_value === "all" ) {
           if( clicked ) {
               $( '#filter input[type=checkbox]' ).prop( 'checked', true );
           } else {
               $( '#filter input[type=checkbox]' ).prop( 'checked', false );
           }
       } else {
           if ( clicked ) {
               // how many agents are enabled
                if ( !$( '#list_filter_agents li input[type=checkbox]:not(:checked)' ).length ) {
                    // check "all
                    $( '#title_filter_agents input[type=checkbox]' ).prop( 'checked', true );
                }
           } else {
               // uncheck "all"
                $( '#title_filter_agents input[type=checkbox]' ).prop( 'checked', false );
           }
       }
       
       filter_by_agents();
    });
    init(); // start
});

