/*
 * Copyright © 2017-2018 Harvard Pilgrim Health Care Institute (HPHCI) and its Contributors.
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
 * of the Software, and to permit persons to whom the Software is furnished to do so, subject to the
 * following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial
 * portions of the Software.
 *
 * Funding Source: Food and Drug Administration ("Funding Agency") effective 18 September 2014 as Contract no.
 * HHSF22320140030I/HHSF22301006T (the "Prime Contract").
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NON-INFRINGEMENT. IN NO EVENT SHALL
 * THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR
 * OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 */
package com.hphc.mystudies.studyAppModule.custom.question;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.support.v7.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;

import com.hphc.mystudies.R;
import com.hphc.mystudies.studyAppModule.custom.AnswerFormatCustom;
import com.hphc.mystudies.studyAppModule.custom.QuestionStepCustom;
import com.hphc.mystudies.utils.AppController;

import org.researchstack.backbone.result.StepResult;
import org.researchstack.backbone.step.Step;
import org.researchstack.backbone.ui.step.body.BodyAnswer;
import org.researchstack.backbone.ui.step.body.StepBody;
import org.researchstack.backbone.utils.FormatHelper;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Calendar;

public class CustomDateQuestionBody implements StepBody {
    //-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*
    // Static Fields
    //-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*


    //-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*
    // Constructor Fields
    //-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*
    private QuestionStepCustom step;
    private StepResult<String> result;
    private DateAnswerformatCustom format;
    private Calendar calendar;
    private DateFormat dateformatter;

    private boolean hasChosenDate;

    public CustomDateQuestionBody(Step step, StepResult result) {
        this.step = (QuestionStepCustom) step;
        this.result = result == null ? new StepResult<>(step) : result;
        this.format = (DateAnswerformatCustom) this.step.getAnswerFormat1();
        this.calendar = Calendar.getInstance();

        if (format.getStyle() == AnswerFormatCustom.DateAnswerStyle.DateAndTime) {
            this.dateformatter = FormatHelper.getFormat(DateFormat.MEDIUM, DateFormat.MEDIUM);
        } else if (format.getStyle() == AnswerFormatCustom.DateAnswerStyle.Date) {
            this.dateformatter = FormatHelper.getFormat(DateFormat.MEDIUM, FormatHelper.NONE);
        } else if (format.getStyle() == AnswerFormatCustom.DateAnswerStyle.TimeOfDay) {
            this.dateformatter = FormatHelper.getFormat(FormatHelper.NONE, DateFormat.MEDIUM);
        }

        // First check the result and restore last picked date
        String savedTime = this.result.getResult();
        if (savedTime != null) {
            try {
                calendar.setTime(AppController.getDateFormat().parse(savedTime));
            } catch (ParseException e) {
            }
            hasChosenDate = true;
        }

        // If no result, use default date if available
        else if (format.getDefaultDate() != null) {
            calendar.setTime(format.getDefaultDate());
        }

        // otherwise, make sure user has made a selection before moving on
        else {
            hasChosenDate = false;
        }
    }

    @Override
    public View getBodyView(int viewType, final LayoutInflater inflater, ViewGroup parent) {
        View view = inflater.inflate(R.layout.rsb_item_date_view, parent, false);

        TextView title = (TextView) view.findViewById(R.id.label);
        if (viewType == VIEW_TYPE_COMPACT) {
            title.setText(step.getTitle());
        } else {
            title.setVisibility(View.GONE);
        }

        final TextView textView = (TextView) view.findViewById(R.id.value);
        textView.setSingleLine(true);
        if (step.getPlaceholder() != null) {
            textView.setHint(step.getPlaceholder());
        } else {
            if (format.getStyle() == AnswerFormatCustom.DateAnswerStyle.Date) {
                textView.setHint(R.string.rsb_hint_step_body_date);
            } else if (format.getStyle() == AnswerFormatCustom.DateAnswerStyle.TimeOfDay) {
                textView.setHint(R.string.rsb_hint_step_body_time);
            } else if (format.getStyle() == AnswerFormatCustom.DateAnswerStyle.DateAndTime) {
                textView.setHint(R.string.rsb_hint_step_body_datetime);
            }
        }

        if (hasChosenDate) {
            textView.setText(createFormattedResult());
        }

        textView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    CustomDateQuestionBody.this.showDialog(textView, inflater);
                }
            }
        });

        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v.isFocused()) {
                    CustomDateQuestionBody.this.showDialog(textView, inflater);
                }
            }
        });

        Resources res = parent.getResources();
        LinearLayout.MarginLayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.leftMargin = res.getDimensionPixelSize(R.dimen.rsb_margin_left);
        layoutParams.rightMargin = res.getDimensionPixelSize(R.dimen.rsb_margin_right);
        view.setLayoutParams(layoutParams);

        return view;
    }

    @Override
    public StepResult getStepResult(boolean skipped) {
        if (skipped) {
            result.setResult(null);
        } else {
            if (hasChosenDate)
                result.setResult(AppController.getDateFormat().format(calendar.getTime()));
            else
                result.setResult(null);
        }

        return result;
    }

    /**
     * @return {@link BodyAnswer#VALID} if result date is between min and max (inclusive) date set
     * within the Step.AnswerFormat
     */
    @Override
    public BodyAnswer getBodyAnswerState() {
        if (!hasChosenDate) {
            return new BodyAnswer(false, R.string.rsb_invalid_answer_date_none);
        }

        return format.validateAnswer(calendar.getTime());
    }

    private void showDialog(final TextView tv, final LayoutInflater inflater) {
        // need to find a material date picker, since it's not in the support library
        final ContextThemeWrapper contextWrapper = new ContextThemeWrapper(tv.getContext(),
                R.style.Theme_Backbone);
        if (format.getStyle() == AnswerFormatCustom.DateAnswerStyle.Date) {
            final DatePickerDialog datePickerDialog = new DatePickerDialog(contextWrapper,
                    new DatePickerDialog.OnDateSetListener() {
                        @Override
                        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        }
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH));
            datePickerDialog.setButton(DialogInterface.BUTTON_NEGATIVE, inflater.getContext().getString(R.string.cancel), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    if (which == DialogInterface.BUTTON_NEGATIVE) {
                        dialog.dismiss();
                    }
                }
            });
            datePickerDialog.setButton(DialogInterface.BUTTON_POSITIVE, inflater.getContext().getString(R.string.ok), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    if (which == DialogInterface.BUTTON_POSITIVE) {
                        dialog.dismiss();
                        hasChosenDate = true;

                        calendar.set(datePickerDialog.getDatePicker().getYear(), datePickerDialog.getDatePicker().getMonth(), datePickerDialog.getDatePicker().getDayOfMonth());
                        // Set result to our edit text
                        String formattedResult = CustomDateQuestionBody.this.createFormattedResult();
                        tv.setText(formattedResult);
                    }
                }
            });
            datePickerDialog.setButton(DialogInterface.BUTTON_NEUTRAL, inflater.getContext().getString(R.string.clear), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    if (which == DialogInterface.BUTTON_NEUTRAL) {
                        dialog.dismiss();
                        tv.setText("");
                        hasChosenDate = false;
                    }
                }
            });
            datePickerDialog.show();
        } else if (format.getStyle() == AnswerFormatCustom.DateAnswerStyle.TimeOfDay) {
            TimePickerDialog timePickerDialog = new TimePickerDialog(contextWrapper,
                    new TimePickerDialog.OnTimeSetListener() {
                        @Override
                        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                            calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                            calendar.set(Calendar.MINUTE, minute);

                            hasChosenDate = true;
                            // Set result to our edit text
                            String formattedResult = CustomDateQuestionBody.this.createFormattedResult();
                            tv.setText(formattedResult);
                        }
                    },
                    calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.MINUTE),
                    true);
            timePickerDialog.setButton(DialogInterface.BUTTON_NEGATIVE, inflater.getContext().getString(R.string.cancel), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    if (which == DialogInterface.BUTTON_NEGATIVE) {
                        dialog.dismiss();
                    }
                }
            });
            timePickerDialog.setButton(DialogInterface.BUTTON_NEUTRAL, inflater.getContext().getString(R.string.clear), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    if (which == DialogInterface.BUTTON_NEUTRAL) {
                        dialog.dismiss();
                        tv.setText("");
                        hasChosenDate = false;
                    }
                }
            });


            timePickerDialog.show();

        } else if (format.getStyle() == AnswerFormatCustom.DateAnswerStyle.DateAndTime) {
            final DatePickerDialog datePickerDialog = new DatePickerDialog(contextWrapper,
                    new DatePickerDialog.OnDateSetListener() {
                        @Override
                        public void onDateSet(DatePicker dview, int year, int monthOfYear, int dayOfMonth) {
                        }
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH));
            datePickerDialog.setButton(DialogInterface.BUTTON_NEGATIVE, inflater.getContext().getString(R.string.cancel), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    if (which == DialogInterface.BUTTON_NEGATIVE) {
                        dialog.dismiss();
                    }
                }
            });
            datePickerDialog.setButton(DialogInterface.BUTTON_NEUTRAL, inflater.getContext().getString(R.string.clear), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    if (which == DialogInterface.BUTTON_NEUTRAL) {
                        dialog.dismiss();
                        tv.setText("");
                        hasChosenDate = false;
                    }
                }
            });
            datePickerDialog.setButton(DialogInterface.BUTTON_POSITIVE, inflater.getContext().getString(R.string.ok), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    if (which == DialogInterface.BUTTON_POSITIVE) {
                        dialog.dismiss();
                        calendar.set(datePickerDialog.getDatePicker().getYear(), datePickerDialog.getDatePicker().getMonth(), datePickerDialog.getDatePicker().getDayOfMonth());

                        TimePickerDialog timePickerDialog = new TimePickerDialog(contextWrapper,
                                new TimePickerDialog.OnTimeSetListener() {
                                    @Override
                                    public void onTimeSet(TimePicker tview, int hourOfDay, int minute) {

                                        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                                        calendar.set(Calendar.MINUTE, minute);

                                        hasChosenDate = true;
                                        // Set result to our edit text
                                        String formattedResult = CustomDateQuestionBody.this.createFormattedResult();
                                        tv.setText(formattedResult);
                                    }
                                },
                                calendar.get(Calendar.HOUR_OF_DAY),
                                calendar.get(Calendar.MINUTE),
                                true);
                        timePickerDialog.setButton(DialogInterface.BUTTON_NEGATIVE, inflater.getContext().getString(R.string.cancel), new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                if (which == DialogInterface.BUTTON_NEGATIVE) {
                                    dialog.dismiss();
                                }
                            }
                        });
                        timePickerDialog.setButton(DialogInterface.BUTTON_NEUTRAL, inflater.getContext().getString(R.string.clear), new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                if (which == DialogInterface.BUTTON_NEUTRAL) {
                                    dialog.dismiss();
                                    tv.setText("");
                                    hasChosenDate = false;
                                }
                            }
                        });
                        timePickerDialog.show();
                    }
                }
            });
            datePickerDialog.show();
        } else {
            throw new RuntimeException("DateAnswerStyle " + format.getStyle() + " is not recognised");
        }
    }

    private String createFormattedResult() {
        return dateformatter.format(calendar.getTime());
    }

}
