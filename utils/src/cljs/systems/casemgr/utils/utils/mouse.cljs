(ns systems.casemgr.utils.utils.mouse)

;; mouse functions
(defn calc-mouse-pos-x [current-mouse-position width]
  (let [screen (.-screen js/window)
        availWidth (.-availWidth screen)
        ;$(window).scrollTop() and $(window).scrollLeft() into consideration
        left (- current-mouse-position (/ width 2))
        right-edge (+ width left)]
    ;(println "width:" width "availWidth:" availWidth "current-mouse-position-x:" current-mouse-position "left:" left "right-edge:" right-edge)
    (if (> right-edge availWidth) (- availWidth width) (if (< left 0) 0 left))
    ))

(defn calc-mouse-pos-y [current-mouse-position height]
  (let [screen (.-screen js/window)
        scrollTop (.-scrolltop js/window)
        availHeight (.-availHeight screen)
        top (- current-mouse-position (/ height 2))
        top-edge (- top height)]
    ;(println "current-mouse-position-y:" current-mouse-position "height:" height "top:" top "availHeight:" availHeight "top-edge:" top-edge "scrollTop:" scrollTop)
    (if (< top 0) 0 top)
    ))

(defn get-mouse-position [event]
  (let [page-x (.-pageX event)
        page-y (.-pageY event)
        client-x (.-clientX event)
        client-y (.-clientY event)
        screen-x (.-screenX event)
        screen-y (.-screenY event)]
    ;(println "page-x:" page-x "client-x:" client-x "screen-x:" screen-x "page-y:" page-y "client-y:" client-y "screen-y:" screen-y)
    {:page-x   page-x
     :client-x client-x
     :screen-x screen-x
     :page-y   page-y
     :client-y client-y
     :screen-y screen-y}))

