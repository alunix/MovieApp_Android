package com.mycompany.tuts;

import android.animation.ObjectAnimator;
import android.support.v7.widget.RecyclerView;

/**
 * Used for animation. Animating custom rows in RecyclerView for this application.
 */
public class AnimationUtils {
//Class has setters and getters
    //Create the boolean to pass so can identify animation position on translationY
    //animates coordinates along a Path using two properties
    public static void animate(RecyclerView.ViewHolder holder, boolean goesDown){
        //specify the target, the property, start, end - ternary operator to set animation
        ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(holder.itemView, "translationY" , goesDown == true ? 300 : -300, 0);

        objectAnimator.setDuration(700);
        objectAnimator.start();
    }
}
