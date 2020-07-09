# Jetpack Compose Date and Time Picker

**Current Compose Version: dev14**

## Download

[ ![Download](https://api.bintray.com/packages/vanpra/ComposeDateTimePicker/datetimepicker/images/download.svg?version=0.1.2) ](https://bintray.com/vanpra/ComposeDateTimePicker/datetimepicker/0.1.2/link)

```gradle
dependencies {
  ...
  implementation 'com.vanpra:datetimepicker:0.1.2'
  ...
}
```

# Usage

### Date and Time Picker

![](https://raw.githubusercontent.com/vanpra/ComposeDateTimePicker/master/imgs/datetime.jpg)

```kotlin
val showingDateTime = state { false }
val currentDateTime = state { LocalDateTime.now() }

DateTimePicker(
    showing = showingDateTime,
    // An optional parameter that sets the date shown at the start - Defaults to the current date and time if not set
    initialDateTime = LocalDateTime.of(2020, 10, 10, 5, 20), 
    onComplete = { dateTime ->
        // Do stuff with java.time.LocalDateTime object which is passed in
    })
```

### Date Picker

![](https://raw.githubusercontent.com/vanpra/ComposeDateTimePicker/master/imgs/date.jpg)

```kotlin
val showingDate = state { false }

DatePicker(
    showing = showingDate,
    initialDate = LocalDate.of(2020, 10, 10), // An optional parameter that sets the date shown at the start
    onComplete = { date ->
        // Do stuff with java.time.LocalDate object which is passed in
    })

```

To show the dialog just set the value of  the state `showingDate` to true

### Time Picker

![](https://raw.githubusercontent.com/vanpra/ComposeDateTimePicker/master/imgs/time.jpg)

```kotlin
val showingTime = state { false }

TimePicker(
    showing = showingTime,
    initialTime = LocalTime.of(5, 20), // An optional parameter that sets the time shown at the start
    onComplete = { time ->
        // Do stuff with java.time.LocalTime object which is passed in
    })

```

To show the dialog just set the value of  the state `showingTime` to true



### Change initial date

To use the date last inputted by the user as the starting point you can make use of the `initialDateTime` parameter along with a mutable state object to keep track of the last selected date. Here is an example of such usage with the DateTime picker.

```kotlin
val selectedDateTime = state { LocalDateTime.now() }

DateTimePicker(
    showing = showingDateTime,
    initialDateTime = selectedDateTime.value,
    onComplete = {
        selectedDate.value = it.toString()
        selectedDateTime.value = it
    })
```



# To Do

1. Limit date selection range (ie. min/max date)

2.  Implement Date range selection 

3. Implement year selection

   
