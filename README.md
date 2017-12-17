# transecon
Modeling tool for transactionally based economic behavior


Trans-Econ is a transactionally based economic simulator/model.  It uses an asyncronous Actor based approach to 
create the ability to simulate, rather than to strictly model economic behavior.  

## Actor types
There are 3 three basic actor types:
1) __Housholds__ - basic driver of consumption and suppliers of Labor to the Production units
1) __Producers/Suppliers__ - basic supplier of goods that are demanded by households (and eventually other suppliers as part of a supply chain)
1) __Markets__ - centers for transactions to take place, roughly equivalent to retailers

## Turns
The complete set of actors, representing all three types, are driven by "turns" which are deisgned to represent a unit of time, for example a payroll cycle such as a month, week, or so on.  

## Actor roles and capabilities and Enhancements
### __Households__ 
have a set capacity to supply labor and a set of demands.
A turn for a household consists of determining monthly needs and purchasing those as economically as possible.

Future __Household__ enhancements will allow 
1) savings
1) ownership levels of market and producer actors
1) variant capacity too supply types of labor at different level of productivity

### __Producers__
Provide payroll to Households
Provide goods to Markets to satisfy Household demands



## Flows
There are two basic flows in this system that generally move in opposite directions.  
### Money
  Money flows from Producers to Households to MArkets to Producers.
  The Bank will eventually be able to supply credit/loans to allow for bridging of financial shortcomings
### Goods
  Goods are produced from inputs, including Labor, which acts as a good.  Goods are then shipped to Markets and purchased and consumed by Households
  
