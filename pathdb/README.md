# pathdatabase

The Path Database is a hierarchical database built out of a hierarchy of maps based on a each node having a :path.

## Overview

### Warning: this is alpha quality, if that, software

The main design goal is to store data in a Tree structure and retrieve it without having to walk the entire tree.   
The solution is to "index" each "node" using a :path keyword.

For instance, selecting /Shops/shop0001/cust0001/veh0002/job0001
   
from:

```clojure
{"Shops"
 {:path "/Shops",
  "shop0001"
  {:path "/Shops/shop0001",
   "cust0001"
   {:path "/Shops/shop0001/cust0001",
    "veh0001"
    {:path "/Shops/shop0001/cust0001/veh0001",
     "job0002"
     {:path "/Shops/shop0001/cust0001/veh0001/job0002",
      :job-id "job0002",
      :hours 3,
      "task0001"
      {:path "/Shops/shop0001/cust0001/veh0001/job0002/task0001",
       :notes "notes.1",
       :hours 3.5,
       :sched-date 20150417},
      "task0004"
      {:path "/Shops/shop0001/cust0001/veh0001/job0002/task0004",
       :notes "notes.4",
       :hours 5,
       :sched-date 20150418}},
     :make "Ford"},
    :last-name "Stang",
    :first-name "Mark",
    "veh0002"
    {:path "/Shops/shop0001/cust0001/veh0002",
     :make "VW",
     "job0001"
     {:path "/Shops/shop0001/cust0001/veh0002/job0001",
      :old-path "/Shops/shop0001/cust0001/veh0001/job0001",
      :job-id "job0001",
      :hours 4,
      :notes "notes.veh0002",
      "task0002"
      {:path "/Shops/shop0001/cust0001/veh0002/job0001/task0002",
       :notes "notes.2",
       :hours 53.5,
       :sched-date 20150417},
      "task0003"
      {:path "/Shops/shop0001/cust0001/veh0002/job0001/task0003",
       :notes "notes.3", 
       :hours 5.5, 
       :sched-date 20150418}}}}}}}
```

yields:

```clojure
    [{:path "/Shops/shop0001/cust0001/veh0002/job0001",
      :old-path "/Shops/shop0001/cust0001/veh0001/job0001",
      :job-id "job0001",
      :hours 4,
      :notes "notes.veh0002",
      "task0002"
      {:path "/Shops/shop0001/cust0001/veh0002/job0001/task0002",
       :notes "notes.2",
       :hours 53.5,
       :sched-date 20150417},
      "task0003"
      {:path "/Shops/shop0001/cust0001/veh0002/job0001/task0003",
       :notes "notes.3", 
       :hours 5.5, 
       :sched-date 20150418}}]
```

## Motivation
When storing data in a ClojureScript Application, we typically have one "atom" of data (i.e. app).
This can be a listandhash (i.e. http://martinfowler.com/bliki/ListAndHash.html), however that means
finding the "list" you are looking for (i.e. employees).  Or it means that you have to have a reference to "employees".
With the pathdatabase, all you need is a path (i.e. /Shops/shop0001/employees).

In addition, Content Management Systems (CMS) store their data in a hierarchical format.  Doing a select from a CMS
will return a list of matching maps.  By using the :path value we can store and retrieve this data and maintain 
the hierarchical relationships.

## Indices
Ideally, indices would be maintained automatically, however, so far they have to be created manually.  In the tests.cljs,
there is a sample index function "re-index" that indexes based on a "task".  The result is:

```clojure
   "indices"
   {"task_indices"
    {20150417
     [{:sched-date 20150417,
       :task-path "/Shops/shop0001/cust0001/veh0001/job0002/task0001"}
      {:sched-date 20150417,
       :task-path
       "/Shops/shop0001/cust0001/veh0001/job0001/task0002"}],
     20150418
     [{:sched-date 20150418,
       :task-path "/Shops/shop0001/cust0001/veh0001/job0002/task0004"}
      {:sched-date 20150418,
       :task-path
       "/Shops/shop0001/cust0001/veh0001/job0001/task0003"}]}}}}}
```
There are now "indices" for two sched-dates 20150417 and 20150418.  We can query the index for 20150417 and we get:

```clojure
[{:sched-date 20150417,
  :task-path "/Shops/shop0001/cust0001/veh0001/job0002/task0001"}
 {:sched-date 20150417,
  :task-path "/Shops/shop0001/cust0001/veh0001/job0001/task0002"}]
```

We can now pull these two tasks without having to search the entire pathdatabase.  However, since it is just 
hierarchical maps, we could also "walk the tree".
  
## Setup

To get an interactive development environment run:

    lein figwheel

and open your browser at [localhost:3449](http://localhost:3449/).
This will auto compile and send all changes to the browser without the
need to reload. After the compilation process is complete, you will
get a Browser Connected REPL. An easy way to try it is:

    (js/alert "Am I connected?")

and you should see an alert in the browser window.

To clean all compiled files:

    lein clean

To create a production build run:

    lein cljsbuild once min

And open your browser in `resources/public/index.html`. You will not
get live reloading, nor a REPL. 

## License

Copyright © 2015 Cybershop Systems

Distributed under the Apache License, see the LICENSE file.
