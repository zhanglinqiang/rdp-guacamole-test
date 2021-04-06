package model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class KeyboardChar {
    private char ch;
    private boolean usingShift;
    private int keycode;
}
