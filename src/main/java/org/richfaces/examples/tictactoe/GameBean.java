/*******************************************************************************
 * JBoss, Home of Professional Open Source
 * Copyright 2010-2011, Red Hat, Inc. and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 *******************************************************************************/
package org.richfaces.examples.tictactoe;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;

import javax.faces.model.SelectItem;

/**
 * The main managed bean. It stores the playing area, information about players and game logic.
 * 
 * @author <a href="mailto:ppitonak@redhat.com">Pavol Pitonak</a>
 * @version $Revision$
 */
@ManagedBean
@SessionScoped
public class GameBean implements Serializable {

    // components on the page that need to be rerendered
    private Set<String> render;
    // number of image on which the user clicked
    private int imageNumber;
    // number of player who clicked (0 or 1)
    private int player = 0;
    private Player[] players;
    // URLs of images that should be rendered
    private String[] images;
    // number of player that won - 9 for no winner
    private int gameFinished = 9;
    // how many clicks were performed
    private int clicks = 0;
    private int games = 0;
    private int ties = 0;
    private Random random;
    private SelectItem[] playersItems = new SelectItem[]{
        new SelectItem(Player.HUMAN_PLAYER, "human"),
        new SelectItem(Player.COMPUTER_PLAYER, "computer")
    };

    public GameBean() {
        images = new String[9];
        random = new Random(9);
        players = new Player[2];

        players[0] = new Player("Player 1", Player.HUMAN_PLAYER, 'x', true);
        players[1] = new Player("Player 2", Player.COMPUTER_PLAYER, 'o', false);

        for (int i = 0; i < 9; i++) {
            images[i] = "clear.png";
        }
        render = new HashSet<String>();
    }

    public String[] getImages() {
        return images;
    }

    public void setImages(String[] images) {
        this.images = images;
    }

    public Set<String> getRender() {
        return render;
    }

    public void setRender(Set<String> reRender) {
        this.render = reRender;
    }

    public int getImageNumber() {
        return imageNumber;
    }

    public void setImageNumber(int imageNumber) {
        this.imageNumber = imageNumber;
    }

    public Player[] getPlayers() {
        return players;
    }

    public void setPlayers(Player[] players) {
        this.players = players;
    }

    public SelectItem[] getPlayersItems() {
        return playersItems;
    }

    public int getGames() {
        return games;
    }

    public void setGames(int games) {
        this.games = games;
    }

    public int getPlayer() {
        return player;
    }

    public void setPlayer(int player) {
        this.player = player;
    }

    public int getTies() {
        return ties;
    }

    public void setTies(int ties) {
        this.ties = ties;
    }

    public String click() {
        System.out.println("clicked on image " + imageNumber);
        if (players[player].getType() == Player.HUMAN_PLAYER) {
            render.clear();
        }

        if (gameFinished == 0 || gameFinished == 1 || clicks == 9) {
            return null;
        }

        // if player click into clear field, perform click, otherwise ignore click
        if ("clear.png".equals(images[imageNumber])) {
            images[imageNumber] = players[player].getCharacter() + ".png";
            render.add("image" + imageNumber);

            checkLine(imageNumber / 3);
            checkColumn(imageNumber % 3);
            if (imageNumber % 2 == 0) {
                checkDiagonals();
            }

            clicks++;
            if (clicks == 9 && gameFinished != 0 && gameFinished != 1) {
                games++;
                ties++;
                render.add("statsForm");
                return null;
            }
            changePlayer();
        }

        if (gameFinished != 0 && gameFinished != 1 && players[player].getType() == Player.COMPUTER_PLAYER && clicks != 9) {
            do {
                imageNumber = random.nextInt(9);
            } while (!images[imageNumber].equals("clear.png"));
            click();
        }

        return null;
    }

    public String newGame() {
        render.clear();
        for (int i = 0; i < 9; i++) {
            images[i] = "clear.png";
        }
        gameFinished = 9;
        player = (players[0].isFirst() ? 0 : 1);
        clicks = 0;

        if (players[player].getType() == Player.COMPUTER_PLAYER) {
            imageNumber = random.nextInt(9);
            click();
        }

        return null;
    }

    public String selectFirstPlayer() {
        players[0].setFirst(player == 0 ? true : false);
        players[1].setFirst(player == 1 ? true : false);

        return newGame();
    }

    public String clearStats() {
        games = 0;
        ties = 0;
        players[0].setWins(0);
        players[1].setWins(0);

        return null;
    }

    /**
     * Checks whether the selected line wins.
     * 
     * @param index
     *            the number of line (0 to 2)
     */
    private void checkLine(int index) {
        if (images[3 * index + 0].equals(images[3 * index + 1]) && images[3 * index + 1].equals(images[3 * index + 2])) {
            for (int i = 0; i < 3; i++) {
                images[3 * index + i] = players[player].getCharacter() + "-win.png";
                render.add("image" + (3 * index + i));
            }
            render.add("statsForm");
            finishGame();
        }
    }

    /**
     * Checks whether the selected column wins.
     * 
     * @param index
     *            the number of column (0 to 2)
     */
    private void checkColumn(int index) {
        if (images[index + 0].equals(images[index + 3]) && images[index + 3].equals(images[index + 6])) {
            for (int i = 0; i < 7; i += 3) {
                images[index + i] = players[player].getCharacter() + "-win.png";
                render.add("image" + (index + i));
            }
            render.add("statsForm");
            finishGame();
        }
    }

    /**
     * Checks whether diagonals win.
     */
    private void checkDiagonals() {
        char tmp = players[player].getCharacter();

        if (images[0].equals(images[4]) && images[4].equals(images[8]) && !images[0].equals("resources/images/clear.png")) {
            images[0] = "resources/images/" + tmp + "-win.png";
            render.add("image0");
            images[4] = "resources/images/" + tmp + "-win.png";
            render.add("image4");
            images[8] = "resources/images/" + tmp + "-win.png";
            render.add("image8");
            render.add("statsForm");
            finishGame();
        }

        if (images[2].equals(images[4]) && images[4].equals(images[6]) && !images[2].equals("images/clear.png")) {
            images[2] = "resources/images/" + tmp + "-win.png";
            render.add("image2");
            images[4] = "resources/images/" + tmp + "-win.png";
            render.add("image4");
            images[6] = "resources/images/" + tmp + "-win.png";
            render.add("image6");
            render.add("statsForm");
            finishGame();
        }
    }

    private void changePlayer() {
        player = player == 0 ? 1 : 0;
    }

    private void finishGame() {
        gameFinished = player == 0 ? 0 : 1;
        players[player].setWins(players[player].getWins() + 1);
        games++;
    }
}
